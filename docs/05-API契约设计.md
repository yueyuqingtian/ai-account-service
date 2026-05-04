# API 契约设计

## 1. 统一规范

### 基础约定

- 协议：HTTPS
- 编码：UTF-8
- 数据格式：JSON
- 时间格式：ISO 8601 或 `yyyy-MM-dd HH:mm:ss`
- 认证方式：Bearer Token

### 统一响应结构

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### 分页结构

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "pageNo": 1,
    "pageSize": 10,
    "total": 100,
    "records": []
  }
}
```

## 2. 用户端接口

## 2.1 认证接口

### POST `/api/auth/register`

请求：

```json
{
  "username": "demo_user",
  "password": "12345678",
  "email": "demo@example.com",
  "verifyCode": "1234"
}
```

### POST `/api/auth/login`

请求：

```json
{
  "username": "demo_user",
  "password": "12345678"
}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "userInfo": {
      "id": 1,
      "username": "demo_user"
    }
  }
}
```

## 2.2 商品接口

### GET `/api/products`

查询参数：

- `pageNo`
- `pageSize`
- `keyword`
- `status`

### GET `/api/products/{id}`

返回商品详情、价格、说明、交付类型、FAQ。

## 2.3 订单接口

### POST `/api/orders`

请求：

```json
{
  "productId": 1,
  "quantity": 1,
  "clientType": "WEB"
}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderNo": "O202605010001",
    "amount": 99.00,
    "status": "UNPAID"
  }
}
```

### GET `/api/user/orders`

查询当前登录用户订单列表。

### GET `/api/user/orders/{orderNo}`

查询订单详情。

## 2.4 支付接口

### POST `/api/payments/create`

请求：

```json
{
  "orderNo": "O202605010001",
  "channel": "ALIPAY"
}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "paymentNo": "P202605010001",
    "payUrl": "https://pay.example.com/redirect"
  }
}
```

### GET `/api/payments/status/{orderNo}`

查询支付状态。

## 2.5 卡密接口

### GET `/api/user/cdkeys`

查询用户卡密列表。

### GET `/api/user/cdkeys/{cdkCode}`

查询卡密详情。

## 2.6 兑换接口

### POST `/api/redeem`

请求：

```json
{
  "cdkCode": "ABCD-EFGH-IJKL"
}
```

成功返回：

```json
{
  "code": 0,
  "message": "兑换成功",
  "data": {
    "deliveryType": "ACCOUNT",
    "account": "user@example.com",
    "password": "******",
    "notice": "请尽快登录并修改安全设置"
  }
}
```

失败场景：

- CDKey 不存在
- CDKey 已使用
- CDKey 已过期
- 库存不足
- 风控拦截

### GET `/api/user/redeems`

查询当前用户兑换记录。

## 3. 管理后台接口

## 3.1 商品管理

- `GET /admin/products`
- `POST /admin/products`
- `PUT /admin/products/{id}`
- `POST /admin/products/{id}/on-shelf`
- `POST /admin/products/{id}/off-shelf`

### 新增商品请求示例

```json
{
  "name": "权益兑换卡",
  "productCode": "PROD_001",
  "price": 99.00,
  "originalPrice": 129.00,
  "deliveryType": "CDKEY",
  "status": "ON_SHELF",
  "description": "商品说明"
}
```

## 3.2 订单管理

- `GET /admin/orders`
- `GET /admin/orders/{orderNo}`
- `POST /admin/orders/{orderNo}/close`
- `POST /admin/orders/{orderNo}/redeliver`

## 3.3 支付管理

- `GET /admin/payments`
- `GET /admin/payments/{paymentNo}`
- `GET /admin/payments/{paymentNo}/callbacks`

## 3.4 CDKey 管理

- `GET /admin/cdkeys`
- `POST /admin/cdkeys/generate`
- `POST /admin/cdkeys/{id}/invalidate`
- `POST /admin/cdkeys/{id}/lock`

## 3.5 库存管理

- `GET /admin/inventory`
- `POST /admin/inventory/import`
- `POST /admin/inventory/{id}/disable`
- `POST /admin/inventory/{id}/enable`

## 3.6 兑换记录管理

- `GET /admin/redeems`
- `GET /admin/redeems/{id}`

## 3.7 风控管理

- `GET /admin/risk/events`
- `POST /admin/risk/blacklist/ip`
- `POST /admin/risk/rules`

## 4. 错误码设计

- `10001`：未登录
- `10002`：token 无效
- `20001`：商品不存在
- `20002`：商品已下架
- `20003`：订单不存在
- `30001`：支付单不存在
- `30002`：支付回调验签失败
- `40001`：CDKey 不存在
- `40002`：CDKey 已使用
- `40003`：CDKey 已过期
- `40004`：库存不足
- `40005`：兑换被风控拦截
- `50001`：管理员权限不足
- `50002`：库存导入失败

## 5. 版本管理建议

- 第一阶段统一使用 `/api` 与 `/admin`
- 当接口不兼容升级时，引入 `/api/v2`
- 前后端共享 DTO 定义时，确保字段命名与枚举值严格一致
