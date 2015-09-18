package spring.osgi.config;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by nico.
 */
public interface OsgiConfiguration {
    final String SERVICE_ID_PROPERTY_KEY = "serviceId";


    void setupConfiguration(OsgiInjector injector);

    public static final class DictionaryBuilder {

        private Dictionary<String, String> dictionary = new Hashtable<>();
        public static DictionaryBuilder dictionary(){
            return new DictionaryBuilder();
        }

        public DictionaryBuilder with(String key, String value){
            dictionary.put(key, value);
            return this;
        }
        public Dictionary<String, String> build(){
            return dictionary;
        }


    }
}
