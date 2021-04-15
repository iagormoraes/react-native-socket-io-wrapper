import { NativeModules, NativeEventEmitter } from 'react-native';

type SocketNativeEvent = {
  data: any;
  eventName: string;
  uniqueID: string;
};

type SocketCallbackResponse = (error?: Error) => void;

export type SocketIOOptions = {
  transports?: string[];
  forceNew?: boolean;
  multiplex?: boolean;
  reconnectionAttempts?: number;
  reconnectionDelay?: number;
  reconnectionDelayMax?: number;
  randomizationFactor?: number;
  timeout?: number;
  query?: string;
  path?: string;
};

export type SocketIOEventData = any;

export type SocketConnectedCallback = (connected: boolean) => void;
export type SocketIdCallback = (id?: string) => void;

export type SocketIOModuleType = {
  initialize(
    url: string,
    options: SocketIOOptions,
    callbackResponse: SocketCallbackResponse
  ): void;
  connect(path: string): void;
  disconnect(path: string): void;
  emit(path: string, eventName: string, options: SocketIOEventData): void;
  on(path: string, eventName: string, callback: Function): void;
  once(path: string, eventName: string, callback: Function): void;
  off(path: string, eventName: string, uniqueID: string): void;
  connected(path: string, callback: SocketConnectedCallback): void;
  connectedSync(path: string): boolean;
  getId(path: string, callback: SocketIdCallback): void;
  getIdSync(path: string): string | null;
};

class SocketIO {
  private SocketIOModule: SocketIOModuleType;
  private readonly SocketIOCallbacksList: {
    [uniqueID: string]: {
      uniqueID: string;
      eventName: string;
      callback: Function;
    };
  };
  eventEmitter: NativeEventEmitter;
  path: string;
  options: SocketIOOptions;

  constructor(url: string, options?: SocketIOOptions) {
    this.SocketIOModule = NativeModules.RNSocketIO;
    this.SocketIOCallbacksList = {};
    this.path = options?.path || '/socket.io';
    console.log(this.path);
    this.options = { ...options, path: this.path };

    this.eventEmitter = new NativeEventEmitter(NativeModules.SocketIo);

    this.eventEmitter.addListener(
      'onEventListener',
      (nativeEvent: SocketNativeEvent) => {
        this.SocketIOCallbacksList[nativeEvent.uniqueID]?.callback(
          nativeEvent.data
        );
      }
    );

    this.SocketIOModule.initialize(
      url,
      this.options,
      this._callCallbackResponse
    );
  }

  _callCallbackResponse(error?: Error) {
    if (error) {
      throw error;
    }
  }

  /**
   * Open socket connection.
   */
  connect() {
    this.SocketIOModule.connect(this.path);
  }

  /**
   * Close socket connection.
   */
  disconnect() {
    this.SocketIOModule.disconnect(this.path);
  }

  /**
   * Send socket event.
   * @param eventName Name of socket event.
   * @param data Data to send on socket event.
   */
  emit(eventName: string, data?: SocketIOEventData) {
    this.SocketIOModule.emit(this.path, eventName, { data });
  }

  /**
   * Listen to socket event.
   * @param eventName Name of socket event.
   * @param callback Callback to listen to socket event.
   */
  on(eventName: string, callback: Function) {
    this.SocketIOModule.on(
      this.path,
      eventName,
      (nativeEventName: string, uniqueID: string) => {
        this.SocketIOCallbacksList[uniqueID] = {
          uniqueID,
          eventName: nativeEventName,
          callback,
        };
      }
    );
  }

  /**
   * Listen once to socket event.
   * @param eventName Name of socket event.
   * @param callback Callback to listen to socket event.
   */
  once(eventName: string, callback: Function) {
    this.SocketIOModule.once(
      this.path,
      eventName,
      (nativeEventName: string, uniqueID: string) => {
        this.SocketIOCallbacksList[uniqueID] = {
          uniqueID,
          eventName: nativeEventName,
          callback,
        };
      }
    );
  }

  /**
   * Remove socket event listener.
   * @param eventName Name of socket event.
   * @param callback Callback of registered socket event.
   */
  off(eventName: string, callback: Function) {
    let keyToDelete: string | null = null;

    for (const key of Object.keys(this.SocketIOCallbacksList)) {
      const listItem = this.SocketIOCallbacksList[key];

      if (listItem.eventName === eventName && listItem.callback === callback) {
        keyToDelete = listItem.uniqueID;

        this.SocketIOModule.off(this.path, eventName, keyToDelete);

        break;
      }
    }

    if (keyToDelete) {
      delete this.SocketIOCallbacksList[keyToDelete];
    }
  }

  /**
   * Get connection status of socket.
   * @param callback Callback with connection status of socket.
   */
  connected(callback: SocketConnectedCallback) {
    this.SocketIOModule.connected(this.path, callback);
  }

  /**
   * Get id of socket.
   * @param callback Callback with id of socket.
   */
  getId(callback: SocketIdCallback) {
    this.SocketIOModule.getId(this.path, callback);
  }

  /**
   * Get connection status of socket.
   * Warning: this method are synchronous blocking UI, use it carefully.
   */
  connectedSync() {
    return this.SocketIOModule.connectedSync(this.path);
  }

  /**
   * Get id of socket.
   * Warning: this method are synchronous blocking UI, use it carefully.
   */
  getIdSync() {
    return this.SocketIOModule.getIdSync(this.path);
  }

  static serializeQuery(object: any) {
    let str = [];

    for (const p in object)
      if (object.hasOwnProperty(p)) {
        str.push(encodeURIComponent(p) + '=' + encodeURIComponent(object[p]));
      }
    return str.join('&');
  }
}

export default SocketIO;
