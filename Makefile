SHELL = /bin/bash

CONTAINER_ENGINE ?= docker
BUILD_IMG ?= registry.aliyuncs.com/zhangguanzhang/gomobile
CONTAINER_ENGINE_RUN_FLAGS ?= -e GOPROXY='https://goproxy.cn,direct'

.PHONY: image
image:
	$(CONTAINER_ENGINE) build . -t $(BUILD_IMG)

.PHONY: shell
shell:
	$(CONTAINER_ENGINE) run $(CONTAINER_ENGINE_RUN_FLAGS) \
		-ti --rm \
		-v $(CURDIR):/go/src/$(PROJECT) \
        -w /go/src/$(PROJECT) \
        --entrypoint bash \
		$(BUILD_IMG)

.PHONY: tun2socks
tun2socks:
	cd tun2socks && \
        go get golang.org/x/mobile/bind && \
        gomobile bind -o $(CURDIR)/app/libs/tun2socks.aar -target android github.com/xjasonlyu/tun2socks/v2/engine
