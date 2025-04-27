## 关于

想劫持封包做一些验证，而 github 上没有 jetpack compose 开发的。
1. 基于 [tun2socks](https://github.com/xjasonlyu/tun2socks) 开发
2. 点击切换选中配置，双击修改，长按删除，勾选指定 app 走代理

不提供编译成品，以及目前有些细节问题暂不修复

## 编译 tun2socks

编译 tun2socks 的 aar

```
make shell
make tun2socks
```

## 免责声明

本程序进攻学习交流参考，请勿用于非法用户

## 参考

- [VpnService](https://developer.android.google.cn/guide/topics/connectivity/vpn#lifecycle)
- [android-vpnservice-example](https://github.com/mightofcode/android-vpnservice-example)
