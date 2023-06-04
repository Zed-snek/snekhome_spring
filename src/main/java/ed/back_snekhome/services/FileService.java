package ed.back_snekhome.services;

import ed.back_snekhome.utils.GenerationFunctions;
import org.apache.commons.io.IOUtils;;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    @Value("${upload.path}")
    private String uploadPath;

    public byte[] getImageByName(String imageName) throws IOException {

        Path destination;
        if (imageName.length() < 10) {
            destination = Paths.get(uploadPath + "systemImages/" + imageName);
        }
        else {
            destination = Paths.get(uploadPath + imageName);
        }

        return IOUtils.toByteArray(destination.toUri());
    }

    public String uploadImageNameReturned(MultipartFile file) throws IOException { //returns new file name

        String name = GenerationFunctions.generateCode(5) + System.currentTimeMillis();

        name += getFileExtension( file.getOriginalFilename() );

        file.transferTo( new File( uploadPath + name ) );

        return name;
    }


    private String getFileExtension( String fileName ) {
        int lastDot = fileName.lastIndexOf('.');

        if ( lastDot == -1 ) //if dot was not found, method lastIndexOf returns value "-1"
            return "";
        else
            return fileName.substring( lastDot );
    }

}
