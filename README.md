# 1. 配置插件

## 1. 在跟项目的`build.gradle`文件中添加插件

```groovy
buildscript {
    repositories {
        maven { url "https://jitpack.io" }
    }

    dependencies {
        classpath 'com.github.coofee:RewritePlugin:<latest version>'
    }
}
```

最新版本：[![](https://jitpack.io/v/coofee/RewritePlugin.svg)](https://jitpack.io/#coofee/RewritePlugin)

## 2. 在`Application`项目中添加如下配置


```groovy
apply plugin: 'com.coofee.rewrite'

rewrite {

    reflect {
        // 启用后，编译应用时会扫描`classAndMethods`中配置的方法，生成的文件结果保存在：/build/intermediates/transforms/rewrite/debug/reflect.json中。
        // 注意：正常编译运行应用时，需要把这个选项关闭掉，否则应用启动会直接崩溃。
        enable = true

        // 类名：方法名列表
        classAndMethods = [
             "android/content/pm/PackageManager" : ["getInstalledApplications", "getInstalledPackages"] as Set
        ]

        myAppPackages = [
                "com/coofee/rewrite/",
        ]
    }

    replaceMethod {
        // 启用后编译应用时会替换除`excludes`包之外的其他类的方法，可以根据`[RewritePlugin] replace`关键字过滤输出日志获取结果。
        enable = true

        // 不替换该包下的方法
        excludes = [
                'com/coofee/rewrite/hook/'
        ]

        // 配置要替换的方法
        configFile = file("replace_method.json")
    }
}
```

## 3. 执行

注意执行插件前，需要先`clean`，否会因为缓存使用旧结果。
```shell
$ ./gradlew clean
$ ./gradlew :app:assembleDebug
```

# 2. 例子

## 1. reflect（扫描隐私API方法调用）

目前扫描了定位、IMEI、IMSI、MAC地址等，如果需要扫描其他方法，直接在`classAndMethods`中配置即可。

> 注意：正常开发时，需要将`reflect`关闭掉，开启它之后可能会导致应用运行时崩溃。

```groovy
reflect {
    enable = true

    classAndMethods = [
            /*************************************************** 应用列表 (START) ***************************************************/

            "android/content/pm/PackageManager" : ["getInstalledApplications", "getInstalledPackages"] as Set,

            /*************************************************** 应用列表 (END) ***************************************************/

            /*************************************************** 定位权限 (START) ***************************************************/
            // 系统定位
            "android/location/LocationManager"         : ["requestLocationUpdates", "requestSingleUpdate"] as Set,

            // IMEI & IMSI 定位：getCellLocation; getDeviceId/getSubscriberId/：READ_PHONE_STATE
            "android/telephony/TelephonyManager"       : ["getCellLocation", "getAllCellInfo", "requestCellInfoUpdate", "requestNetworkScan", "getDeviceId", "getSubscriberId", "getImei"] as Set,

            // 高德地图
            "com/amap/api/location/AMapLocationClient" : ["startLocation", "startAssistantLocation"] as Set,
            // 百度地图
            "com/baidu/location/LocationClient"        : ["start"] as Set,
            /*************************************************** 定位权限 (END) ***************************************************/

            /*************************************************** MacAddress (START) ***************************************************/

            "android/net/wifi/WifiInfo"                : ["getMacAddress"] as Set,
            "java/net/NetworkInterface"                : ["getHardwareAddress"] as Set,

            /*************************************************** MacAddress (END) ***************************************************/
    ]

    myAppPackages = [
            "com/coofee/rewrite/",
    ]

}
```

* 扫描结果

[reflect结果.json](./Rewrite插件数据/reflect.json)


## 2. replaceMethod（替换方法）

在`replace_method.json`文件中配置要替换的方法，编译时会使用`dest_class.dest_method`替换`src_class.src_method`对应的方法。

```groovy
replaceMethod {
    // 启用后编译应用时会替换除`excludes`包之外的其他类的方法，可以根据`[RewritePlugin] replace`关键字过滤输出日志获取结果。
    enable = true

    // 不替换该包下的方法
    excludes = [
            'com/coofee/rewrite/hook/'
    ]

    // 配置要替换的方法
    configFile = file("replace_method.json")
}
```

`replace_method.json`文件是json数组，其结构如下所示，替换隐私API的方法配置详见：[replace_method.json](./Rewrite插件数据/replace_method.json)

```json
[
  {
    "src_class": "android.telephony.TelephonyManager",  
    "dest_class": "com.coofee.rewrite.hook.telephony.ShadowTelephoneManager",
    "methods": [
      {
        "src_method": "java.lang.String getDeviceId()",
        "dest_method": "java.lang.String getDeviceId(android.telephony.TelephonyManager)"
      },
      {
        "src_method": "java.lang.String getDeviceId(int)",
        "dest_method": "java.lang.String getDeviceId(android.telephony.TelephonyManager, int)"
      }
    ]
  }
]
```


* 结果查看：

可以使用`[RewritePlugin] replace`关键字过滤输出日志获取替换结果，单条输出如下所示：

```
[RewritePlugin] replace moduleName=8f0a287af65fe4840370804c25783e3d59e2e135, sourceFile=MainActivity.kt, lineNo=61, className=com.coofee.rewrite.MainActivity, methodName=testPackageManager, methodDesc=()V, methodSignature=null; owner=android/content/pm/PackageManager, method=getInstalledApplications, desc=(I)Ljava/util/List; by owner=com/coofee/rewrite/hook/pm/ShadowPackageManager, method=getInstalledApplications, desc=(Landroid/content/pm/PackageManager;I)Ljava/util/List;
```

* `replace_method`编写方法

1. 如果`src_method`是实例方法，则其对应的`dest_method`静态方法的第一个参数是实例自身，也就是`this`。
2. 如果`src_method`是静态方法，则其对应的`dest_method`静态方法和其一模一样。
3. 如果`src_method`方法存在泛型，在需要去掉其限定类型，见：PackageManager配置。

配置例子详见：[replace_method.json](./app/replace_method.json)
