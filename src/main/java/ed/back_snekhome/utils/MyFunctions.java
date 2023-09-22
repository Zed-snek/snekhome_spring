package ed.back_snekhome.utils;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

public class MyFunctions {

    public static String generateCode( int length ){
        Random rand = new Random();
        int c;
        StringBuilder s = new StringBuilder();

        for ( int i = 0; i < length; i++ ){
            c = rand.nextInt(5);
            switch (c) {
                case 0 -> s.append((char) rand.nextInt(48, 58)); //ascii numbers
                case 1, 2 -> s.append((char) rand.nextInt(97, 123)); //ascii letters
                case 3, 4 -> s.append((char) rand.nextInt(65, 91)); //ascii big letters
            }
        }
        return s.toString();
    }

    public static <V> void setIfNotNull(V value, Consumer<V> setter) {
        if (Objects.nonNull(value)) {
            setter.accept(value);
        }
    }

    public static <V> void setIfNotEquals(V value, V value2, Consumer<V> setter) {
        if (!Objects.equals(value, value2)) {
            setter.accept(value2);
        }
    }


}

