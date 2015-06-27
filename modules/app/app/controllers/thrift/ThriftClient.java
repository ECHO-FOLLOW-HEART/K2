//package controllers.thrift;
//
//import com.aizou.yunkai.userservice;
//import org.apache.thrift.protocol.*;
//import org.apache.thrift.transport.TSocket;
//import org.apache.thrift.transport.TTransport;
//import org.apache.thrift.transport.TTransportException;
//
///**
// * Created by topy on 2015/6/13.
// */
//public class ThriftClient {
//
////    public void startClient() {
////        TTransport transport = null;
////        try {
////            transport = new TSocket("localhost", 1234);
////            TProtocol protocol = new TBinaryProtocol(transport);
////            userservice.Client client = new userservice.Client(protocol);
////            transport.open();
////        } catch (TTransportException e) {
////            e.printStackTrace();
////        } finally {
////            transport.close();
////        }
////    }
//
////    public static void main(String[] args) {
////        userservice.Client client = new Client();
////        client.startClient();
////    }
//}
