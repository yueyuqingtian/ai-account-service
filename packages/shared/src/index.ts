export type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

export type PageResponse<T> = {
  pageNo: number
  pageSize: number
  total: number
  records: T[]
}

export const paymentChannels = [
  { label: '支付宝', value: 'ALIPAY' },
  { label: '微信支付', value: 'WECHAT' },
  { label: '开发 Mock', value: 'MOCK' }
]
