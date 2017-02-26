import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by impy on 25.02.17.
 */
public class Test {
    public int foo(){return 1;}
    public static void main(String[] args) {
        Class c = Integer.class;
        for(Method m : c.getDeclaredMethods()) {
            System.out.println(m);
        }
    }
}
