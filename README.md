# react-native-socket-io

A native implementation of Socket.io for React Native.

⚠️ Currently working only on Android, the next commits will focus on iOS.

⚠️ Android uses `io.socket:socket.io-client:1.0.1` which supports only 2x socket.io server.

## Installation

```sh
npm install react-native-socket-io
```

## Usage

```js
import SocketIO from "react-native-socket-io";

// ...
const socketIO = new SocketIO('http://127.0.0.1:3000', {
  transports: ['websocket'],
  query: SocketIO.serializeQuery({
    token: 'Bearer JWT',
  }),
});
```
⚠️ For more info, please see in example of Android the usage of the socket with Hooks and lifecycle.

## Methods (incomplete, please see example)

### connect
```js
socketIO.connect();
```
Open the connection of socket instance.

### disconnect
```js
socketIO.disconnect();
```
Close the connection of socket instance.

### on
```js
socketIO.on(eventName, callback);
```
Listen to the socket event.

#### Props

``eventName: string``
``callback: Function``

### emit
```js
socketIO.emit(eventName, data);
```
Send a socket event.

#### Props

``eventName: string``
``data: any``

## Todos

- Write comment for each function to assist newbies
- Write tests
- Implement iOS native module

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
