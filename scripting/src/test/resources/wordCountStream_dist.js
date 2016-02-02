// mode=distributed,language=javascript
var Function = Java.type("java.util.function.Function")
var Collectors = Java.type("java.util.stream.Collectors")
var Arrays = Java.type("org.infinispan.scripting.utils.JSArrays")
/*cache
    .entrySet().stream()
    .map(function(e) e.getValue())
    .map(function(v) v.toLowerCase())
    .map(function(v) v.split(/[\W]+/))
    .flatMap(function(f) Arrays.stream(f))
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));*/

    cache.entrySet().stream()
                    .map((Serializable & Function<Map.Entry<String, String>, String[]>) e -> e.getValue().split(/[\\W]+/))
                    .flatMap((Serializable & Function<String[], Stream<String>>) Arrays::stream)
                    .collect(CacheCollectors.serializableCollector(
                            () -> Collectors.groupingBy(Function.identity(), Collectors.counting())))