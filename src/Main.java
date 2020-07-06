import com.heapsimulation.*;

public class Main {

    public static void main(String[] args) {
        if(args != null && args.length > 0){
            CommandProcessor processor = new CommandProcessor();
            processor.processFile(args[0]);
        }
    }
}
