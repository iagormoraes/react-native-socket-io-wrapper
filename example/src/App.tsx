import * as React from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import SocketIO from 'react-native-socket-io-wrapper';
import makeSocketIO from './SocketIO';

function useSocketIO() {
  const [socketIO, setSocketIO] = React.useState(() => {
    return makeSocketIO({
      forceNew: true,
      transports: ['websocket'],
      query: SocketIO.serializeQuery({
        token: 'Bearer JWT2',
      }),
    });
  });

  const [value, setValue] = React.useState({
    title: '',
    body: '',
    message: '',
    balance: 0,
  });

  const onMessage = React.useCallback((message) => {
    setValue(message);
  }, []);

  const onConnection = React.useCallback(
    (data) => {
      console.log('onConnection', data);

      socketIO.emit('send_number', 124.25);
      socketIO.emit('send_string', '123');
      socketIO.emit('send_null', null);
      socketIO.emit('send_boolean', false);
      socketIO.emit('send_map', {
        numberVal: 123,
        double: 123.25,
        bool: false,
        nullable: null,
        string: '123',
        list: [
          {
            numberVal: 123,
            double: 123.25,
            bool: false,
            nullable: null,
            string: '123',
          },
        ],
      });
      socketIO.emit('send_array', [
        {
          numberVal: 123,
          double: 123.25,
          bool: false,
          nullable: null,
          string: '123',
          list: [
            {
              numberVal: 123,
              double: 123.25,
              bool: false,
              nullable: null,
              string: '123',
            },
          ],
        },
      ]);
    },
    [socketIO]
  );

  const onReconnectAttempt = React.useCallback((data) => {
    console.log('onReconnectAttempt', data);
  }, []);

  const onDataReceived = React.useCallback((data) => {
    console.log('dataReceived', typeof data, data);
  }, []);

  const onError = React.useCallback(
    (error) => {
      console.log(error);
      if (error === 'invalid JWT') {
        socketIO.disconnect();

        setTimeout(() => {
          setSocketIO(
            makeSocketIO({
              forceNew: true,
              transports: ['websocket'],
              query: SocketIO.serializeQuery({
                token: 'Bearer JWT',
              }),
            })
          );
        }, 2000);
      }
    },
    [socketIO]
  );

  React.useEffect(() => {
    socketIO.connect();

    socketIO.on('connect', onConnection);
    // socketIO.on('reconnect_attempt', onReconnectAttempt);
    // socketIO.on('send_number', onDataReceived);
    // socketIO.on('send_string', onDataReceived);
    // socketIO.on('send_null', onDataReceived);
    // socketIO.on('send_boolean', onDataReceived);
    // socketIO.on('send_map', onDataReceived);
    // socketIO.on('send_array', onDataReceived);
    socketIO.on('error', onError);

    socketIO.on('receive_message', onMessage);

    return () => {
      socketIO.disconnect();

      socketIO.off('connect', onConnection);
      // socketIO.off('reconnect_attempt', onReconnectAttempt);
      // socketIO.off('send_number', onDataReceived);
      // socketIO.off('send_string', onDataReceived);
      // socketIO.off('send_null', onDataReceived);
      // socketIO.off('send_boolean', onDataReceived);
      // socketIO.off('send_array', onDataReceived);
      socketIO.off('error', onError);
      socketIO.off('receive_message', onMessage);
    };
  }, [
    socketIO,
    onConnection,
    onReconnectAttempt,
    onDataReceived,
    onError,
    onMessage,
  ]);

  return {
    value,
  };
}

export default function App() {
  const { value } = useSocketIO();

  return (
    <View style={styles.container}>
      <ScrollView>
        {Array(50)
          .fill(undefined)
          .map((_, index) => (
            <React.Fragment key={index}>
              <View style={styles.container}>
                <Text style={styles.title}>{value.title}</Text>
                <Text>{value.body}</Text>
                <Text>{value.message}</Text>
              </View>
            </React.Fragment>
          ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { margin: 8, padding: 8, backgroundColor: '#cc8fdd' },
  title: { fontSize: 26 },
});
