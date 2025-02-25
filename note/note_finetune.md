### 准备工作
- 下载模型：https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct/tree/main
- 数据集：multi-class-1000x10.json
- 代码：FT-Qwen.ipynb
（模型、数据集和代码放在同一目录下）

### 训练参数调节
- lora参数调整：
```python
config = LoraConfig(
    task_type=TaskType.CAUSAL_LM,
    target_modules=["q_proj", "k_proj", "v_proj", "o_proj", "gate_proj", "up_proj", "down_proj", "lm_head"],
    inference_mode=False,  # 训练模式
    r=8,  # Lora 秩
    lora_alpha=32,  # Lora alaph，具体作用参见 Lora 原理
    lora_dropout=0.1,  # Dropout 比例
)
```

- 训练参数调整：
```python 
args = TrainingArguments(
    output_dir="./output/multi-class-20epoch",  # 保存的模型输出位置
    per_device_train_batch_size=4,  # batchsize
    gradient_accumulation_steps=4,  # 每4个batch更新一次梯度
    logging_steps=2500,             # 打印日志
    num_train_epochs=20,            # 训练轮次
    save_steps=2500,                # 保存检查点
    learning_rate=1e-4,             # 学习率
    save_on_each_node=True,
    gradient_checkpointing=True,
    report_to="none",
)
```

其他的部分都不用调整，直接运行就行。


### 微调后的模型评估
代码: Test-Qwen.ipynb（与上述代码至于同一目录下）
测试集：eval-400x10.json

```python 
model = PeftModel.from_pretrained(
    model,
    model_id="./output/multi-class-20epoch/checkpoint-7500/", # 此处改为保存的检查点位置
)
```

**现在遇到的问题是**：
注释下面这行代码（分别加载base_model和LoRA适配器，可以得到预期的模型回复）
但是运行下面这行代码（合并权重）之后再次调用，模型的回复就会出现一堆乱码

```python
model = model.merge_and_unload()
```

0.5B模型的大小是最适合部署的，也是最能体现微调前后效果对比的（因为微调之前0.5b的表现极差），**希望可以优先解决上述问题**（目前猜测的错误原因是合并时浮点数溢出（是否能换用更高精度的模型权重表示？）tokenizer编码错误？……


#### 其他
目前成功尝试过的模型部署是mlc提供的量化后的版本
https://huggingface.co/mlc-ai/Qwen2.5-0.5B-Instruct-q4f16_1-MLC/tree/main

微调后的模型如何（量化）转换成mlc格式（后续可采用相同方式部署），目前这一流程还没有打通。