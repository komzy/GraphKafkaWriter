# SendToFlinkPregel
## 概要
KafkaへFlinkPregel用タプルメッセージを送信します。  
CSV形式のノードファイルにノード情報を記載し、CSV形式のシナリオファイルにエッジ情報を記載します。  
シナリオファイルからデータを読み取りノード情報と合成したタプルメッセージ（JSON）を生成しKafkaへ送信します。<br>

<hr>

## 実行方法
### 1. jarコマンドでの実行
Jarファイルアップロード後の実行例を以下に示します。  
実行例）  
`java -jar -Dkafka.topic=kafka -Dkafka.kafkaBootStrapServers=localhost:9092 -Dkafka.dateFormat=yyyy-MM-dd'\''T'\''HH:mm:ss.SSS -Dkafka.nodeFilePath=/tmp/node_blogel_ssp.csv -Dkafka.scenarioFilePath=/tmp/scenario_blogel_ssp.csv SendToFlinkPregel-1.0-jar-with-dependencies.jar`

Mavenからビルドすると以下2個のjarファイルが生成されます。  
"-with-dependencies.jar" の方を指定してください。
- SendToFlinkPregel-1.0.jar
- SendToFlinkPregel-1.0-jar-with-dependencies.jar

#### パラメータの指定方法
##### 1-1. コマンドラインでの指定
上記実行例のように`-D`の次に「パラメータ名=設定値」の形式で指定します。
##### 1-2. kafka.confファイルでの指定
resources/kafka.conf で指定します。  
上記コマンドラインでの指定を行わない場合は、kafka.confファイルの内容で実行されます。

### 2. IntelliJでの実行
SendToFlinkPregelMainがmainクラスになります。  
この中のmain()メソッドを実行してください。

#### パラメータの指定方法
##### 2-1. 実行構成画面での指定
- main()の左側に表示されている▶をクリックし「実行構成の変更」を開く。
- 「プログラムの引数」に以下のように記述。  
  設定例）
  ```
  "-Dkafka.topic=kafka33"
  "-Dkafka.kafkaBootStrapServers=localhost:9092"
  "-Dkafka.dateFormat=yyyy-MM-dd'\''T'\''HH:mm:ss.SSS"
  "-Dkafka.nodeFilePath=/tmp/node_blogel_ssp.csv"
  "-Dkafka.scenarioFilePath=/tmp/scenario_blogel_ssp.csv"
  ```
##### 2-2. kafka.confファイルでの指定
resources/kafka.conf で指定します。  
上記実行構成画面での指定を行わない場合は、kafka.confファイルの内容で実行されます。

<hr>

## 起動パラメータ
kafka.confに記載。-Dで指定も可能。  
以下パラメータ。

### 1. kafka.topic
送信先Topic名。
### 2. kafka.kafkaBootStrapServers
BootStrapServers。
### 3. kafka.dateFormat
タイムスタンプのフォーマット。
### 4. kafka.nodeFilePath
読み込むノードファイルのパス。
### 5. kafka.scenarioFilePath
読み込むシナリオファイルのパス。

<hr>

## シナリオファイル記載方法
CSV形式で記載する。1行に1タプル（エッジ情報）記載。  
記載する1行は下記送信メッセージに対応。
### タプル
* フォーマット
```
<edgeId>, <startNodeId>, <endNodeId>(, <numFeatureList>)(, <textFeatureList>)(, <timestamp>)
```
* パラメータ
  * edgeId : エッジID。int型で記述。
  * startNodeId : 始点ノードID。int型で記述。
  * endNodeId : 終点ノードID。int型で記述。
  * numFeatureList : double型数値を空白区切りで記述。（省略可能）
  * textFeatureList : String型文字列を空白区切りで記述。（省略可能）
  * timestamp : タイムスタンプ。パラメータdateFormatで指定した形式で記述。（省略可能。省略した場合は実行時刻が自動的に付与される。）

### wait文
* フォーマット
```
wait, <time>
```
* パラメータ
  * time : waitする時間。単位：ミリ秒。


<hr>

## ノードファイル記載方法
CSV形式で記載する。1行に1ノード情報記載。  
記載する1行は下記送信メッセージに対応。
- フォーマット
```
<nodeId>(, b-<blockId>)(, <numFeatureList>)(, <textFeatureList>)
```

- パラメータ
  - nodeId : ノードID。int型で記述。
  - b-blockId : ブロックID。"b-ブロックID" の形式で記述。（Pregelの場合は省略する）
  - numFeatureList : double型数値を空白区切りで記述。（省略可能）
  - textFeatureList : String型文字列を空白区切りで記述。（省略可能）
<hr>

## 送信タプルメッセージ
※"blockId" はBlogelの場合のみ。
- フォーマット（JSON形式）
```json
{
  "edgeId " : エッジID,
  "startNode" :　{
    "nodeId" : ノードID,
    "blockId" : ブロックID,
    "numFeatureList" : [数値, 数値, ・・・],
    "textFeatureList" : [文字列, 文字]
  },
  "endNode" :　{
    "nodeId" : ノードID,
    "blockId" : ブロックID,
    "numFeatureList" : [数値, 数値, ・・・],
    "textFeatureList" : [文字列, 文字]
  },
  "numFeatureList" : [数値, 数値, ・・・],
  "texFeatureList" : [文字列, 文字],
  "time" : yyyy-MM-ddTHH:mm:ss.SSS
}
```

<hr>

## サンプルシナリオファイルについて

- scenario_pregel_max.csv : Pregel Max Value、及びPageRankテスト用シナリオ
- scenario_pregel_max_3window.csv : Pregel Max Value、及びPageRankテスト用シナリオ, 3Windowでの動作用
- scenario_pregel_ssp.csv : Pregel Single Shortest Path用シナリオ
- scenario_pregel_ssp_3window.csv : Pregel Single Shortest Path用シナリオ, 3Windowでの動作用 
- scenario_pregel_reachability.csv : Pregel Reachability用シナリオ
- scenario_pregel_reachability_3window.csv : Pregel Reachability用シナリオ, 3Windowでの動作用
- scenario_blogel_max.csv : Blogel Max Valueテスト用シナリオ
- scenario_blogel_max_3window.csv : Blogel Max Valueテスト用シナリオ, 3Windowでの動作用
- scenario_blogel_ssp.csv : Blogel Single Shortest Path用シナリオ（タイムスタンプ指定なし）
- scenario_blogel_ssp_3window.csv : Blogel Single Shortest Path用シナリオ（タイムスタンプ指定なし）, 3Windowでの動作用
- scenario_blogel_ssp_time.csv : Blogel Single Shortest Path用シナリオ（タイムスタンプ指定あり）
- scenario_blogel_reachability.csv : Blogel Reachability用シナリオ
- scenario_blogel_reachability_3window.csv : Blogel Reachability用シナリオ, 3Windowでの動作用

## サンプルノードファイルについて

- node_pregel_max.csv : Pregel Max Value、及びPageRankテスト用ノードファイル
- node_pregel_max_3window.csv : Pregel Max Value、及びPageRankテスト用ノードファイル, 3Windowでの動作用
- node_pregel_ssp.csv : Pregel Single Shortest Path用ノードファイル
- node_pregel_ssp_3window.csv : Pregel Single Shortest Path用ノードファイル, 3Windowでの動作用
- node_pregel_reachability.csv : Pregel Reachability用ノードファイル
- node_pregel_reachability_3window.csv : Pregel Reachability用ノードファイル, 3Windowでの動作用
- node_blogel_max.csv : Blogel Max Valueテスト用ノードファイル
- node_blogel_max_3window.csv : Blogel Max Valueテスト用ノードファイル, 3Windowでの動作用
- node_blogel_ssp.csv : Blogel Single Shortest Path用ノードファイル
- node_blogel_ssp_3window.csv : Blogel Single Shortest Path用ノードファイル, 3Windowでの動作用
- node_blogel_reachability.csv : Blogel Reachability用ノードファイル
- node_blogel_reachability_3window.csv : Blogel Reachability用ノードファイル, 3Windowでの動作用
