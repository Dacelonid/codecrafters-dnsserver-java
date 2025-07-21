package ie.dacelonid.dns;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        Thread thread;
        if(args.length == 2){
            String[] IPandPort = args[1].split(":");
            thread = new Thread(new Server(2053, IPandPort[0], IPandPort[1]));
        }else {
            thread = new Thread(new Server(2053));
        }
        thread.start();
    }
}
