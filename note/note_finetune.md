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
