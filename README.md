## AOP
此项目是旨在学习AOP在Android上的相关技术

### 一. APT

#### 什么是APT？

引用官方文档的描述
> The command-line utility apt, annotation processing tool, finds and executes annotation processors based on the annotations present in the set of specified source files being examined. The annotation processors use a set of reflective APIs and supporting infrastructure to perform their processing of program annotations (JSR 175). The apt reflective APIs provide a build-time, source-based, read-only view of program structure. These reflective APIs are designed to cleanly model the JavaTM programming language's type system after the addition of generics (JSR 14). First, apt runs annotation processors that can produce new source code and other files. Next, apt can cause compilation of both original and generated source files, thus easing the development cycle.

简单来说，APT是一种注解处理工具，它会根据源文件中存在的注释，查找并执行注解处理器。执行注解处理器时，可以生成新的源代码文件和其他文件。APT还会编译原有的源文件和生成的源文件。

APT会在编译期解析注解。所以很多第三方库，如`Dagger2`，`ButterKnife`，`EventBus3`等都使用APT来实现相关功能。

如果对Annotation还不是很了解的话，可以看看这个图
![注解介绍](img/annotation_intro.jpg)

接下来会实现一个类似`ButterKnife`功能的项目`ViewBinder`，通过实现`@BindView`注解来学习APT

#### 项目结构
参考`ButterKnife`的模块划分，新建`ViewBinder`项目，然后新建如下模块：

* `binder` - Android Library，存放对外使用的API
* `binder-annotations` - Java Library，存放注解
* `binder-compiler` - Java Library，存放注解处理器

模块间的依赖关系：

* `binder-compiler` 依赖 `binder-annotations`

#### 创建注解
`@BindView`用于成员变量，并且需要接收一个id作为参数，用于绑定View。

```Java
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface BindView {

    int value();
}
```

#### 创建注解处理器
创建一个`AbstractProcessor`的子类，并重写相关方法
```Java
public class BinderProcessor extends AbstractProcessor {
    private Filer mFiler; //文件相关的辅助类，生成JavaSourceCode
    private Elements mElementUtils; //元素相关的辅助类，帮助我们去获取一些元素相关的信息
    private Messager mMessager; //日志相关的辅助类

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        mFiler = env.getFiler();
        mElementUtils = env.getElementUtils();
        mMessager = env.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        return false;
    }
}
```
所有的注解处理器类都必须有一个无参构造函数。下面介绍下`AbstractProcessor`几个常用的方法：

* `init(ProcessingEnvironment env)`：初始化方法，可以在此获取一些工具类：
    * `Filer`：文件相关的辅助类，生成JavaSourceCode
    * `Elements`：元素相关的辅助类，帮助我们去获取一些元素相关的信息
    * `Messager`：日志相关的辅助类
* `getSupportedAnnotationTypes()`：指定需要被注解处理器注册的注解，即需要处理哪些注解。返回的集合里面是注解类型的全称。
* `getSupportedSourceVersion()`：指定使用的Java版本。通常返回SourceVersion.latestSupported()
* `process(Set<? extends TypeElement> set, RoundEnvironment env)`：最重要的方法。在这个方法里可以对注解进行处理，并生成相应的文件。

另外，如果注解处理器类生成了新的源文件，APT会重复调用`Processor`的方法，直到不再生成新的源文件为止（因为新生成的源文件可能会包含需要被处理的注解）

##### 如何编译Processer?
方法一：  
在Gradle Project面板中，依次选择binder-compiler -> Tasks -> build -> build，双击运行即可

方法二：  
在命令行执行
```
gradlew binder-compiler:build
```

##### 遇到的问题
错误: 编码GBK的不可映射字符  

解决方法：  
在`binder-compiler`的build.gradle文件中，增加
```
tasks.withType(JavaCompile) {
    options.encoding = "UTF-8"
}
```
然后重新sync一下即可

#### 注册注解处理器
方法一：  
创建javax.annotation.processing.Processor文件
与java平级创建resources/META-INF/services/目录

方法二：  
使用Google的[AutoService](https://github.com/google/auto/tree/master/service)。

#### 编写Processor
TODO

参考：

* [Getting Started with the Annotation Processing Tool, apt](https://docs.oracle.com/javase/7/docs/technotes/guides/apt/GettingStarted.html)
* [Android 利用 APT 技术在编译期生成代码](https://brucezz.itscoder.com/use-apt-in-android)
* [javapoet](https://github.com/square/javapoet)
* [ButterKnife](https://github.com/JakeWharton/butterknife)