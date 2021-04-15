import SocketIO, { SocketIOOptions } from 'react-native-socket-io';

function makeSocketIO(options?: SocketIOOptions) {
  return new SocketIO('http://127.0.0.1:3000', options);
}

export default makeSocketIO;
