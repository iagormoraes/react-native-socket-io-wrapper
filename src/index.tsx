import { NativeModules } from 'react-native';

type SocketIoType = {
  multiply(a: number, b: number): Promise<number>;
};

const { SocketIo } = NativeModules;

export default SocketIo as SocketIoType;
