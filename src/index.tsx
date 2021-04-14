import { NativeModules, NativeEventEmitter } from 'react-native';

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
};

type SocketNativeEvent = {
  data: any;
  eventName: string;
  uniqueID: string;
};

type SocketIOEventData = any;

type SocketCallbackResponse = (error?: Error) => void;

export type SocketIOModuleType = {
  initialize(
    url: string,
    options: SocketIOOptions,
    callbackResponse: SocketCallbackResponse
  ): void;
  connect(): void;
  disconnect(): void;
  emit(eventName: string, options: SocketIOEventData): void;
  on(eventName: string, callback: Function): void;
  once(eventName: string, callback: Function): void;
  off(eventName: string, uniqueID: string): void;
};

const SocketIOModule: SocketIOModuleType = NativeModules.SocketIo;

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

  constructor(url: string, options?: SocketIOOptions) {
    this.SocketIOModule = SocketIOModule;
    this.SocketIOCallbacksList = {};

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
      options ?? {},
      this._callCallbackResponse
    );
  }

  _callCallbackResponse(error?: Error) {
    if (error) {
      throw error;
    }
  }

  connect() {
    this.SocketIOModule.connect();
  }

  disconnect() {
    this.SocketIOModule.disconnect();
  }

  emit(eventName: string, data: SocketIOEventData) {
    this.SocketIOModule.emit(eventName, { data });
  }

  on(eventName: string, callback: Function) {
    this.SocketIOModule.on(
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

  once(eventName: string, callback: Function) {
    this.SocketIOModule.once(
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

  off(eventName: string, callback: Function) {
    let keyToDelete: string | null = null;

    for (const key of Object.keys(this.SocketIOCallbacksList)) {
      const listItem = this.SocketIOCallbacksList[key];

      if (listItem.eventName === eventName && listItem.callback === callback) {
        keyToDelete = listItem.uniqueID;

        this.SocketIOModule.off(eventName, keyToDelete);

        break;
      }
    }

    if (keyToDelete) {
      delete this.SocketIOCallbacksList[keyToDelete];
    }
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
