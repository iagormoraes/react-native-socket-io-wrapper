react-native-socket-io-wrapper
======================

A native implementation of Socket.io for React Native.

⚠️ Currently working only on Android, the future commits will focus on iOS.

⚠️ Android uses `io.socket:socket.io-client:1.0.1` which supports `2.x (or 3.1.x / 4.x with allowEIO3: true)` as described at the [compatibility page](https://socketio.github.io/socket.io-client-java/installation.html).

# Motivation

After experiencing some performance problems with the JS socket.io library in React Native context, I decided to write a module using the Java solution that can be used in the UI/background threads and by doing so, keeping the JS thread more open to others expensive works and having the performance 💯, and a more stable development experience.

# Table of contents

- [Installation](#installation)
  - [Adding with TurboReactPackage class](#adding-with-turboReactPackage-class)
- [Usage](#usage)
- [Methods](#methods)
- [Todos](#todos)
- [Contributing](#contributing)
- [License](#license)

## Installation

```sh
npm install react-native-socket-io-wrapper
```

### Adding with TurboReactPackage class

In MainApplication.java add the following:
```
        @Override
        protected List<ReactPackage> getPackages() {
          return Arrays.asList(
            new SocketIoTurboPackage(), // <-- add
            new MainReactPackage()
          );
        }
```


## Usage

```js
import SocketIO from "react-native-socket-io-wrapper";

// ...
const socketIO = new SocketIO('http://127.0.0.1:3000', {
  transports: ['websocket'],
  query: SocketIO.serializeQuery({
    token: 'Bearer JWT',
  }),
});
```
⚠️ For more info, please see in example of Android the usage of the socket with Hooks and lifecycle.

## Methods

### connect
```js
socketIO.connect();
```
Open socket connection.

### disconnect
```js
socketIO.disconnect();
```
Close socket connection.

### on
```js
socketIO.on(eventName, callback);
```
Listen to socket event.

### once
```js
socketIO.once(eventName, callback);
```
Listen once to socket event.

#### Props

``eventName: string``
``callback: Function``

### emit
```js
socketIO.emit(eventName, data);
```
Send socket event.

### off
```js
socketIO.off(eventName, data);
```
Remove socket event listener.

#### Props

``eventName: string``
``data: any``

### connected
```js
socketIO.connected(callback);
```
Get connection status of socket.

### connectedSync
```js
socketIO.connectedSync();
```
Get connection status of socket.

⚠️ this method are synchronous blocking UI, use it carefully.

### getId
```js
socketIO.getId(callback);
```
Get id of socket.

### getIdSync
```js
socketIO.getIdSync();
```
Get id of socket.

⚠️ this method are synchronous blocking UI, use it carefully.

### updateSocketOptions
```js
socketIO.updateSocketOptions(updatedOptions);
```
Update socket options, this updates general instances paths.

### updateSocketOptionsSync
```js
socketIO.updateSocketOptionsSync(updatedOptions);
```
Update socket options, this updates general instances paths.

⚠️ this method are synchronous blocking UI, use it carefully.

## Todos

- Write tests
- Implement iOS native module

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
