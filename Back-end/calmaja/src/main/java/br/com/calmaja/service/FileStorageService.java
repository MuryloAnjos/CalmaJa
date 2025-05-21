package br.com.calmaja.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStoreLocation;

    public FileStorageService(Path fileStoreLocation) {
        this.fileStoreLocation = fileStoreLocation;
    }

    public FileStorageService(){
        this.fileStoreLocation = Paths.get("uploads/profile-images").toAbsolutePath().normalize();
        try{
            Files.createDirectories(this.fileStoreLocation); // cria se nao existir
        }catch (IOException e){
            throw new RuntimeException("Could not create directory for profile images!", e);
        }
    }

    public String storeFile(MultipartFile file, UUID userId){
        if(file.isEmpty()){
            throw new RuntimeException("Cannot upload an empty file!");
        }
        String fileName = userId + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetLocation = this.fileStoreLocation.resolve(fileName); // a localização vai ser o que passou no construtor mais o nome do arquivo

        try {
            // Copia o arquivo para o diretorio passado e substitui caso exista
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;

        }catch (IOException e){
            throw new RuntimeException("Could not store file " + fileName, e);
        }

    }

    //função criada para conseguir acessar o arquivo depois no front
    public Resource loadAsResource(String fileName){
        try{
            Path filePath = this.fileStoreLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()){
                return resource;
            }else{
                throw new RuntimeException("File not found: " + fileName);
            }

        }catch (Exception e){
            throw new RuntimeException("File not found: " + fileName);
        }
    }
}
