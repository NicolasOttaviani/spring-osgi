
package spring.osgi.bundle;

import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import spring.osgi.bundle.annotation.ProvideService;
import spring.osgi.bundle.annotation.RequireService;
import spring.osgi.bundle.annotation.Services;
import spring.osgi.context.internal.OsgiConfigApplicationContext;
import spring.osgi.utils.BaseActivator;
import spring.osgi.utils.OsgiStringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nico.
 */
public class SpringActivator extends BaseActivator {
    private final Class<?> annotatedClass;
    private final ProvideService[] provides;
    private final RequireService[] requires;

    private OsgiConfigApplicationContext applicationContext;

    public SpringActivator(Logger logger, Class<?>  annotatedClass) {
        super(logger);
        Assert.notNull(annotatedClass, "annotated class configuration could not be null");
        this.annotatedClass = annotatedClass;

        Collection<Services> services = collectServicesAnnotations(annotatedClass);
        Set<ProvideService> provideServicesSet = new HashSet<>();
        Set<RequireService> requireServicesSet = new HashSet<>();

        for (Services service: services){
            provideServicesSet.addAll(Arrays.asList(service.provides()));
            requireServicesSet.addAll(Arrays.asList(service.requires()));
        }

        this.provides = provideServicesSet.toArray(new  ProvideService[provideServicesSet.size()]);
        this.requires = requireServicesSet.toArray(new RequireService[requireServicesSet.size()]);
    }

    public SpringActivator(Class<?> annotatedClass) {
        this(null, annotatedClass);
    }

    @Override
    protected void doOpen() throws Exception {
        super.doOpen();
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getBundleClassLoader());
            trackServices();

            logger.info("Starting bundle.");
            if (logger.isDebugEnabled()){
                logger.debug("Tracking services: {}", buildRequireServicesNames());
            }
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    protected void doClose() {
        super.doClose();
        logger.info("Stopping bundle");
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (this.waitingTracker()) {
            return;
        }

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getBundleClassLoader());
            this.applicationContext = new OsgiConfigApplicationContext(bundleContext);
            this.applicationContext.setRequiredServices(this.getServicesResolved());
            this.applicationContext.register(annotatedClass);

            this.applicationContext.refresh();
            registerServices();

            logger.info("Starting Application Context.");
            if (logger.isDebugEnabled()){
                logger.debug("Export services: {}", buildProvideServicesNames());
            }

        } catch (Exception exc) {
            logger.error("Error on starting application context from bundle {}", OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle()), exc);
            this.applicationContext = null;
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    protected void doStop() {
        super.doStop();
        if (this.applicationContext != null) {
            if (this.applicationContext.isActive()) {
                logger.info("Stopping Application Context.");
                this.applicationContext.destroy();
            }
            this.applicationContext = null;
        }
    }

    private void trackServices() throws InvalidSyntaxException {
        for (RequireService service : this.requires) {

            String id = service.id();
            Class<?> clazz = service.value();
            String filter = service.filter();
            if ("".equals(id)) {
                id = clazz.getName();
            }
            if ("".equals(filter)) {
                filter = null;
            }

            trackService(id, clazz, filter);
        }
    }

    private void registerServices() {
        for (ProvideService provideService : this.provides) {
            this.register(provideService.value(), this.applicationContext.getBean(provideService.value()));
        }
    }

    private String buildRequireServicesNames() {
        if (this.requires.length == 0){
            return "none";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i=0; i<this.requires.length; ++i){
            RequireService requireService = this.requires[i];
            if (i > 0){
                stringBuilder.append(", ");
            }
            stringBuilder.append(requireService.value().getName());
            if (!"".equals(requireService.filter())){
                stringBuilder
                        .append("{")
                        .append("filter:")
                        .append(requireService.filter())
                        .append("}");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private String buildProvideServicesNames() {
        if (this.provides.length == 0){
            return "none";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i=0; i< this.provides.length; ++i){
            ProvideService provideService = this.provides[i];
            if (i > 0){
                stringBuilder.append(", ");
            }
            stringBuilder.append(provideService.value().getName());
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private ClassLoader getBundleClassLoader() {
        return annotatedClass.getClassLoader();
    }

    private Collection<Services> collectServicesAnnotations(Class<?> annotatedClass) {
        Set<Class<?>> annotatedClassesVisited = new HashSet<>();
        Set<Services> services = new HashSet<>();
        parseAnnotatedClasses(annotatedClass, services, annotatedClassesVisited);
        return services;
    }

    private void parseAnnotatedClasses(Class<?> annotatedClass, Set<Services> services, Set<Class<?>> annotatedClassesVisited) {
        if (annotatedClassesVisited.contains(annotatedClass)){
            return;
        }
        annotatedClassesVisited.add(annotatedClass);

        Services servicesAnnotation = annotatedClass.getAnnotation(Services.class);
        if (servicesAnnotation != null){
            services.add(servicesAnnotation);
        }

        Import importAnnotation = annotatedClass.getAnnotation(Import.class);
        if (importAnnotation != null){
            for (Class<?> importedAnnotatedClass: importAnnotation.value()){
                parseAnnotatedClasses(importedAnnotatedClass, services, annotatedClassesVisited);
            }
        }
    }

}
