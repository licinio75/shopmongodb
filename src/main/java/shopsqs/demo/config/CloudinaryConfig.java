package shopsqs.demo.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dzqlsz7u8",
                "api_key", "755864121538351",
                "api_secret", "pvZaetg-nlr-GBlrbwqe3MHz3EI"
        ));
    }
}
