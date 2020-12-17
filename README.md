# dart_helper
1.根据Json数据创建 Dart 文件
![add_file.gif](./readme/add_file.gif?raw=true)

2.根据Json数据添加 Dart 类（class）
![add_class.gif](./readme/add_class.gif?raw=true)


###根据Dart Class生成 Json解析函数
支持数据类型
int,double,String,bool,DateTime,List,Map,enum,class

3.添加 formMap函数
![add_form_map.gif](./readme/add_form_map.gif?raw=true)

4.添加 toMap函数
![add_to_map.gif](./readme/add_to_map.gif?raw=true)

5.添加 copyWith函数
![add_copy_with.gif](./readme/add_copy_with.gif?raw=true)

### 的函数体中自动添加Dio网络请求代码
支持数据类型
int,double,String,bool,DateTime,List,Map,enum,class
![add_request.gif](./readme/add_request.gif?raw=true)

### 将代码输出到 Word文档

[下载](./dart_helper.jar?raw=true)

*不能查看图片和下载文件，请将ip地址添加到 hosts文件
*192.30.253.112  github.com
*199.232.4.133   raw.githubusercontent.com

[gitee地址](https://gitee.com/wskfjt/dart_helper)
[github地址](https://github.com/wskfjtheqian/dart_helper)


```dart

enum ETest {
  Test,
}

class TestDartSub {
  factory TestDartSub.fromMap(dynamic map) {
    if (null == map) return null;
    var temp;
    return TestDartSub();
  }

  TestDartSub();
}

class TestDartHelper<T> {
  bool testBool;
  String testString;
  int testInt;
  double testDouble;
  dynamic testDynamic;
  DateTime testDateTime;
  TestDartSub testSub;
  ETest testEnum;
  T testT;
  
  List<bool> listBool;
  List<String> listString;
  List<int> listInt;
  List<double> listDouble;
  List<dynamic> listDynamic;
  List<DateTime> listDateTime;
  List<TestDartSub> listSub;
  List<ETest> listEnum;
  List<T> listT;

  Map<String, bool> mapBool;
  Map<String, String> mapString;
  Map<String, int> mapInt;
  Map<String, double> mapDouble;
  Map<String, dynamic> mapDynamic;
  Map<String, DateTime> mapDateTime;
  Map<String, TestDartSub> mapSub;
  Map<String, ETest> mapEnum;
  Map<String, T> mapT;

  TestDartHelper({
    this.testBool,
    this.testString,
    this.testInt,
    this.testDouble,
    this.testDynamic,
    this.testDateTime,
    this.testSub,
    this.testEnum,
    this.testT,
    this.listBool,
    this.listString,
    this.listInt,
    this.listDouble,
    this.listDynamic,
    this.listDateTime,
    this.listSub,
    this.listEnum,
    this.listT,
    this.mapBool,
    this.mapString,
    this.mapInt,
    this.mapDouble,
    this.mapDynamic,
    this.mapDateTime,
    this.mapSub,
    this.mapEnum,
    this.mapT,
  });

  factory TestDartHelper.fromMap(dynamic map, T Function(dynamic map) callT) {
    if (null == map) return null;
    var temp;
    return TestDartHelper(
      testBool: null == (temp = map['testBool']) ? null : (temp is bool ? temp : (temp is num ? 0 != temp.toInt() : ('true' == temp.toString()))),
      testString: map['testString']?.toString(),
      testInt: null == (temp = map['testInt']) ? null : (temp is num ? temp.toInt() : int.tryParse(temp)),
      testDouble: null == (temp = map['testDouble']) ? null : (temp is num ? temp.toDouble() : double.tryParse(temp)),
      testDynamic: map['testDynamic'],
      testDateTime: null == (temp = map['testDateTime']) ? null : (temp is DateTime ? temp : DateTime.tryParse(temp)),
      testSub: TestDartSub.fromMap(map['testSub']),
      testEnum: null == (temp = map['testEnum']) ? null : (temp is num ? ETest.values[temp.toInt()] : ETest.values[int.tryParse(temp)]),
      testT: callT(map['testT']),
      listBool: null == (temp = map['listBool']) ? [] : (temp is List ? temp.map((map) => null == map ? null : (map is bool ? map : (map is num ? 0 != map.toInt() : ('true' == temp.toString())))).toList() : []),
      listString: null == (temp = map['listString']) ? [] : (temp is List ? temp.map((map) => map?.toString()).toList() : []),
      listInt: null == (temp = map['listInt']) ? [] : (temp is List ? temp.map((map) => null == map ? null : (map is num ? map.toInt() : int.tryParse(map))).toList() : []),
      listDouble: null == (temp = map['listDouble']) ? [] : (temp is List ? temp.map((map) => null == map ? null : (map is num ? map.toDouble() : double.tryParse(map))).toList() : []),
      listDynamic: null == (temp = map['listDynamic']) ? [] : (temp is List ? temp.map((map) => map).toList() : []),
      listDateTime: null == (temp = map['listDateTime']) ? [] : (temp is List ? temp.map((map) => null == map ? null : (map is DateTime ? map : DateTime.tryParse(map))).toList() : []),
      listSub: null == (temp = map['listSub']) ? [] : (temp is List ? temp.map((map) => TestDartSub.fromMap(map)).toList() : []),
      listEnum: null == (temp = map['listEnum']) ? [] : (temp is List ? temp.map((map) => null == map ? null : (map is num ? ETest.values[map.toInt()] : ETest.values[int.tryParse(map)])).toList() : []),
      listT: null == (temp = map['listT']) ? [] : (temp is List ? temp.map((map) => callT(map)).toList() : []),
      mapBool: null == (temp = map['mapBool']) ? {} : (temp is Map ? temp.map((key, map) => MapEntry(key?.toString(), null == map ? null : (map is bool ? map : (map is num ? 0 != map.toInt() : ('true' == temp.toString()))))) : {}),
      mapString: null == (temp = map['mapString']) ? {} : (temp is Map ? temp.map((key, map) => MapEntry(key?.toString(), map?.toString())) : {}),
      mapInt: null == (temp = map['mapInt']) ? {} : (temp is Map ? temp.map((key, map) => MapEntry(key?.toString(), null == map ? null : (map is num ? map.toInt() : int.tryParse(map)))) : {}),
      mapDouble: null == (temp = map['mapDouble']) ? {} : (temp is Map ? temp.map((key, map) => MapEntry(key?.toString(), null == map ? null : (map is num ? map.toDouble() : double.tryParse(map)))) : {}),
      mapDynamic: null == (temp = map['mapDynamic']) ? {} : (temp is Map ? temp.map((key, map) => MapEntry(key?.toString(), map)) : {}),
      mapDateTime: null == (temp = map['mapDateTime']) ? {} : (temp is Map ? temp.map((key, map) => MapEntry(key?.toString(), null == map ? null : (map is DateTime ? map : DateTime.tryParse(map)))) : {}),
      mapSub: null == (temp = map['mapSub']) ? {} : (temp is Map ? temp.map((key, map) => MapEntry(key?.toString(), TestDartSub.fromMap(map))) : {}),
      mapEnum: null == (temp = map['mapEnum']) ? {} : (temp is Map ? temp.map((key, map) => MapEntry(key?.toString(), null == map ? null : (map is num ? ETest.values[map.toInt()] : ETest.values[int.tryParse(map)]))) : {}),
      mapT: null == (temp = map['mapT']) ? {} : (temp is Map ? temp.map((key, map) => MapEntry(key?.toString(), callT(map))) : {}),
    );
  }
}
```