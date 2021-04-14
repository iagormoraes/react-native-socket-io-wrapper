import SocketIO, { SocketIOOptions } from 'react-native-socket-io';

function makeSocketIO(options?: SocketIOOptions) {
  return new SocketIO('http://192.168.0.11:3000', options);
}

export default makeSocketIO;
