package com.yao.food_menu;

import com.yao.food_menu.entity.Dish;
import com.yao.food_menu.mapper.DishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class CheckImageData implements CommandLineRunner {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private com.yao.food_menu.common.config.LocalStorageProperties localProperties;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== IMAGE DATA CHECK ======");
        List<Dish> dishes = dishMapper.selectList(null);
        for (Dish dish : dishes) {
            String name = dish.getName();
            String localImage = dish.getLocalImage();
            String image = dish.getImage();
            System.out.println("Dish: [" + name + "], localImage: [" + localImage + "], image: [" + image + "]");
            
            if (localImage != null && !localImage.isEmpty() && !localImage.startsWith("http")) {
                Path fullPath = Paths.get(localProperties.getBasePath(), localImage);
                boolean exists = new File(fullPath.toString()).exists();
                System.out.println("  File exists: " + exists + " -> " + fullPath);
            }
        }
        System.out.println("==============================");
    }
}
