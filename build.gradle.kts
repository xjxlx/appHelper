import com.android.build.api.dsl.LibraryDefaultConfig

@Suppress("DSL_SCOPE_VIOLATION") plugins {
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.android.library)
    id("maven-publish") //用来推送到jitpack
}

android {
    compileSdk = Config.compileSdk
    defaultConfig {
        minSdk = Config.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // 初始化系统设置
        initSystemInfo(this)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

fun initSystemInfo(build: LibraryDefaultConfig) {
    var system = System.getenv("USERDOMAIN_ROAMINGPROFILE") // windows
    if (system == null) {
        system = System.getenv("USER")// mac
    }
    build.buildConfigField("String", "SYSTEM_NAME", "\"${system}\"")
    println("SYSTEM_NAME:$system")
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 系统级类库
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)

    api(libs.refresh.header.classics)   // 经典刷新头
    api(libs.refresh.layout.kernel)  // 刷新的核心须依赖
    api(libs.refresh.footer.classics)  // 经典加载

    implementation(libs.logger)   // log工具
    // implementation() "com.squareup.okhttp3:logging-interceptor:4.8.0" // 拦截器，非必须

    // 公共的类库引用
    api(libs.lifecycle.viewmodel.ktx)
    api(libs.lifecycle.livedata.ktx)

    // 私有的类库引用
    implementation(libs.eventbus) { isTransitive = false }
    implementation(libs.rxpermissions) { isTransitive = false } // rxjava3 版本

    api(libs.gson)
    api(libs.retrofit2)// 必要retrofit依赖
    api(libs.adapter.rxjava2)  // 必要依赖，和Rxjava结合必须用到，下面会提到
    api(libs.converter.gson) // 必要依赖，解析json字符所用
    api(libs.converter.scalars) // 必要依赖，把数据转产成字符串使用
    api(libs.okhttp)  // okHttp的依赖
    api(libs.rxjava2) // 必要rxjava2依赖
    implementation(libs.rxjava3) { // 禁止依赖的传递
        isTransitive = false
    }

    api(libs.rxandroid)  // 必要rxAndroid依赖，切线程时需要用到
    api(libs.glide)   // glide 图片加载库 ，尽量自己使用，避免版本冲突
    annotationProcessor(libs.glide.compiler)

    api(libs.android.pickerview)  // 日历选择器
    api(libs.viewpager2)
    implementation(project(":common"))

    // room数据库的依赖
//    implementation()("androidx.room:room-runtime:2.4.2") {// 禁止依赖的传递
//        transitive = false
//    }

    // 高德
//    implementation()("com.amap.api():location:5.6.1") {// 定位
//        transitive = false// 禁止依赖的传递
//    }
//    implementation()("com.amap.api():search:8.1.0") {  // 搜索
//        transitive = false// 禁止依赖的传递
//    }
//    implementation()("com.amap.api():3dmap:8.1.0") {  // 地图
//        transitive = false// 禁止依赖的传递
//    }
}

afterEvaluate {
    publishing { // 发布配置
        publications {// 发布内容
            create<MavenPublication>("release") {// 注册一个名字为 release 的发布内容
                // 从当前 module 的 release 包中发布
                from(components["release"])
                groupId = "com.github.apphelper"
                artifactId = "apphelper" // 插件名称
                version = "3.0.7" // 版本号
            }
        }
    }
}


