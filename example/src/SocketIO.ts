import SocketIO from 'react-native-socket-io';

const SocketIOInstance = new SocketIO('http://127.0.0.1:3000', {
  transports: ['websocket'],
  query: SocketIO.serializeQuery({
    token: 'Bearer JWT',
  }),
});

export default SocketIOInstance;
