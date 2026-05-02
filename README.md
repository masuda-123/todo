# Todoアプリ

## 概要
Todoタスクの作成・一覧取得・更新・削除が可能なwebアプリ

## 機能
- タスク一覧取得
- タスク作成（バリデーションあり）
- タスク名更新（バリデーションあり）
- タスクの完了、未完了切り替え（単一、複数可能）
- タスク削除（単一、複数可能）

## 使用技術
- Java 21
- Spring Boot 4.0.2
- Spring Data JPA
- H2（組み込みDB）
- Maven
- JUnit / MockMvc
- HTML5（Thymeleafテンプレート）
- CSS3
- JavaScript（ES6）

## テスト
- Contollerテスト
- Serviceテスト
- Repositoryテスト
- 統合テスト

## 工夫した点
- RESTfulなAPI設計を意識
- MockMvcを使ったテストコードを作成
- 例外発生時のレスポンスを統一し、利用者が扱いやすいよう設計
- javaScriptを利用し、ブラウザ画面からAPIを操作できるUIを作成

## API例

### 作成
POST /todos

request:
```json
{
  "title": "test"
}
```

response:
```json
{
  "id": 1,
  "title": "test",
  "done": false
}
```
