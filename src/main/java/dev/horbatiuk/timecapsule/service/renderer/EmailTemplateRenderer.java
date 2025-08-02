package dev.horbatiuk.timecapsule.service.renderer;

import dev.horbatiuk.timecapsule.service.aws.SendCapsuleEmailHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmailTemplateRenderer {

    private final TemplateEngine templateEngine;

    public EmailTemplateRenderer() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/email/opencapsule/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }


    public String renderEmail(SendCapsuleEmailHandler.CapsuleEmailMetadata metadata, List<URL> downloadUrls) {
        Context context = new Context();
        context.setVariable("username", metadata.username());
        context.setVariable("subject", metadata.title());
        context.setVariable("description", metadata.description());
        context.setVariable("openAt", LocalDateTime.parse(
                metadata.openAt().replace("T", " "),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.S]")
        ));
        context.setVariable("downloadUrls", downloadUrls);
        return templateEngine.process("email-capsule-opened", context);
    }
}
