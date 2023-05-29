package ed.back_snekhome.utils;

import ed.back_snekhome.entities.helpful.Image;

import java.util.List;

public class ListFunctions {

    public static String getTopImageOfList(List<? extends Image> images) {
        if (images.size() == 0) {
            return "";
        }
        else {
            return  images.get( images.size() - 1 ).getName();
        }
    }

}
