### 一、将模型转换成MLC格式

工作目录：`D:\fudan\LLM`

#### 1. 转换权重
```shell
mlc_llm convert_weight ./FineTune-Qwen/Qwen2.5-0.5B-Instruct --quantization q4f16_1 -o Quantization/Qwen2.5-q4f16_1-MLC
```
（生成了`ndarray-cache.json`和`params_shard_x.bin`）
```shell
Parameter size after quantization: 0.259 GB
Total parameters: 494,032,768
Bits per parameter: 4.502
```

*遇到报错删除： `D:\Programs\anaconda3\envs\mlc_llm\lib\site-packages\torch\lib\libiomp5md.dll`*

#### 2. 生成 mlc-chat-config.json
```shell
mlc_llm gen_config ./FineTune-Qwen/Qwen2.5-0.5B-Instruct --quantization q4f16_1 --conv-template qwen2 -o Quantization/Qwen2.5-q4f16_1-MLC
```
（生成了`merges.txt`, `mlc-chat-config.json`, `tokenizer_config.json`, `tokenizer.json`, `vocab.json`）

#### 3. 编译android包(不需要)
```shell
mlc_llm compile Quantization/Qwen2.5-q4f16_1-MLC/mlc-chat-config.json --device android -o Quantization/libs/Qwen2.5-q4f16_1-android.tar
```


### 二、端侧部署
#### 1.上传模型
```shell
git lfs install
git clone https://huggingface.co/tangerine6/tangerine_model
cd tangerine_model
cp D:/fudan/LLM/Quantization/Qwen2.5-q4f16_1-MLC* .
git add .
git commit -m "Add model"
git push origin main
```

#### 2.打包模型
```shell
cd D:/fudan/LLM/onDevice/mlc-llm/android/MLCChat
set MLC_LLM_SOURCE_DIR=D:/fudan/LLM/onDevice/mlc-llm
mlc_llm package
```
首先删除`MLCChat/dist`
更改`mlc-package-config.json`（`model_lib_path_for_prepare_libs`方案不行，需要上传之后再下载的方式）：
```json
{
    "device": "android",
    "model_list": [
        {
            "model": "HF://tangerine6/tangerine_model",
            "estimated_vram_bytes": 1288490240,
            "model_id": "Qwen2.5-ft-q4f16_1-MLC",
            "bundle_weight": true,
            "model_lib": "qwen_ft_q4f16_1"
        }
    ]
}
```