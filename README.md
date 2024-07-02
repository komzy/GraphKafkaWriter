# SendToFlinkPregel
## 概要
KafkaへFlinkPregel用タプルメッセージを送信します。  
CSV形式のノードファイルにノード情報を記載し、CSV形式のシナリオファイルにエッジ情報を記載します。  
シナリオファイルからデータを読み取りノード情報と合成したタプルメッセージ（JSON）を生成しKafkaへ送信します。<br>

<hr>

## シナリオファイル記載方法
CSV形式で記載する。1行に1タプル（エッジ情報）記載。  
記載する1行は下記送信メッセージに対応。
- フォーマット
```
<edgeId>, <startNodeId>, <endNodeId>(, <numFeatureList>)(, <textFeatureList>)(, <timestamp>)
```

- パラメータ
    - edgeId : エッジID。int型で記述。
    - startNodeId : 始点ノードID。int型で記述。
    - endNodeId : 終点ノードID。int型で記述。
    - numFeatureList : double型数値を空白区切りで記述。（省略可能）
    - textFeatureList : String型文字列を空白区切りで記述。（省略可能）
    - timestamp : タイムスタンプ。パラメータdateFormatで指定した形式で記述。（省略可能。省略した場合は実行時刻が自動的に付与される。）

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

## サンプルシナリオファイルについて  

- scenario_pregel_max.csv : Pregel Max Value、及びPageRankテスト用シナリオ
- scenario_pregel_ssp.csv : Pregel Single Shortest Path用シナリオ
- scenario_blogel_max.csv : Blogel Max Valueテスト用シナリオ
- scenario_blogel_ssp.csv : Blogel Single Shortest Path用シナリオ（タイムスタンプ指定なし）
- scenario_blogel_ssp_time.csv : Blogel Single Shortest Path用シナリオ（タイムスタンプ指定あり）

## サンプルノードファイルについて  

- node_pregel_max.csv : Pregel Max Value、及びPageRankテスト用ノードファイル
- node_pregel_ssp.csv : Pregel Single Shortest Path用ノードファイル
- node_blogel_max.csv : Blogel Max Valueテスト用ノードファイル
- node_blogel_ssp.csv : Blogel Single Shortest Path用ノードファイル