# ECO maven repository

## about

各プラグイン単体ビルド用の公開りぽじとり



## example

### maven: pom.xml

```xml:pom.xml
<repositories>
    <repository>
        <id>eco-plugin</id>
        <url>http://ecolight15.github.io/mvn_rep/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>jp.minecraftuser</groupId>
        <artifactId>EcoFramework</artifactId>
        <version>0.2</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```
