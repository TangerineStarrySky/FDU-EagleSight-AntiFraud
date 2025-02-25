# Qwen2.5-0.5b 模型微调

### 简介
此分支包含Qwen2.5-0.5b模型的微调代码，旨在通过LoRA技术对模型进行优化，以提升其在特定任务（如反诈短信识别）中的性能。代码基于PyTorch框架实现，支持模型的加载、微调、评估和保存。

### 技术栈
- **Python**：主要编程语言
- **PyTorch**：深度学习框架
- **Transformers**：Hugging Face的Transformers库用于加载和微调预训练模型
- **LoRA**：Low-Rank Adaptation技术用于模型优化

### 示例代码
#### 微调脚本示例
```python
from transformers import AutoTokenizer, AutoModelForCausalLM
from peft import LoraConfig, TaskType, get_peft_model

# 加载模型和分词器
tokenizer = AutoTokenizer.from_pretrained("./Qwen2.5-0.5B-Instruct")
model = AutoModelForCausalLM.from_pretrained("./Qwen2.5-0.5B-Instruct", device_map="auto", torch_dtype="auto")
# 配置LoRA
config = LoraConfig(
    task_type=TaskType.CAUSAL_LM,
    target_modules=["q_proj", "k_proj", "v_proj", "o_proj", "gate_proj", "up_proj", "down_proj", "lm_head"],
    inference_mode=False,
    r=8,   # Lora 秩
    lora_alpha=32, 
    lora_dropout=0.1,
)
model = get_peft_model(model, lora_config)

```

### 微调数据集主要来源
- [Abooooo/FGRC-SCD](https://huggingface.co/datasets/Abooooo/FGRC-SCD)
- [Telecom_Fraud_Texts_8](https://github.com/ChangMianRen/Telecom_Fraud_Texts_8)

感谢开源！

