[![Build Status](https://travis-ci.org/Jxin-Cai/mdd.svg?branch=master)](https://travis-ci.org/Jxin-Cai/mdd)
[![codecov](https://codecov.io/gh/Jxin-Cai/mdd/branch/master/graph/badge.svg)](https://codecov.io/gh/Jxin-Cai/mdd/)
### 一.开篇词
技术有两种，一种是做单点突破，像语音、搜索、算法。第二是**处理复杂流程**。这是两种一样难的事情。
-- 行癫
### 二、项目介绍
本项目是对模型驱动设计的实践项目。
* data-model   以数据模型驱动设计去实现需求。
* domain-model 以领域驱动设计去实现需求。

注: 本项目旨在通过应用和输出提升个人对架构设计的认知。文中的大部分阐述都是以个人理解为模板，并没有照搬书中知识点。故自然会有错误和不足。若有大佬慧眼如炬，将项目中不足或错误的地方加以补充或修正，将万分感谢。

### 三、建模视角

* 数据模型: 以数据为核心，关注数据实体的样式和它们之间的关系
* 服务模型: 以为系统外部的客户端提供的服务为核心，关注客户端发起的请求以及服务返回的响应
* 领域模型: 以领域为核心，通过识别领域对象来表达业务系统的领域知识包括业务流程、业务规则和约束关系

注: 不同视角意味着以不同的模型为核心,但不是说其他模型就不存在了。事实上我们的项目中有时会同时存在dto、dmo、do,只是侧重点不同罢了。

### 四、建模过程

#### 1.建模过程图示

![建模过程](https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/modeling_process.png)

#### 2.建模过程阶段介绍

* 分析阶段(提取): 基于现实世界的业务需求，依据设计者的建模观点对业务知识进行提炼与转换，形成表达了业务规则、业务流程或业务关系的逻辑概念
* 设计阶段(精炼): 运用软件设计方法进一步提炼与转换分析模型中的逻辑概念，使得模型在满足需求功能的同时满足更高的设计质量
* 实现阶段(翻译): 通过编码对设计模型中的概念进行提炼与转换，构建可以运行的高质量软件，同时满足未来的需求变更与产品维护


### 五、MDD(度量驱动开发/业务)

#### 1.性能指标
* 并发量
* 响应耗时
* 成功率
* 资源(cpu,内存,磁盘,带宽)占用情况
#### 2.业务指标
* 根据具体业务情况

#### 3.精益创业模型

<img src="https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/lean_startup.jpg" height="350" width="500">

### 六、TDD(测试驱动开发)
#### 1.简介
* 代码层次：在编码之前写测试脚本，可以称为单元测试驱动开发
* 业务层次：在需求分析时就确定需求（如用户故事）的**验收标准**

#### 2.基础原则
* 通过所有测试(Passes its tests)
* 尽可能消除重复 (Minimizes duplication)
* 尽可能清晰表达 (Maximizes clarity)
* 更少代码元素 (Has fewer elements)

#### 3.测试驱动开发模型

<img src="https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/tdd.jpg" height="350" width="500">

### 七、能力评估模型

引用下张逸大佬的能力评估模型:

#### 1.敏捷迭代能力
| 等级 | 团队 | 需求 |过程|
| --- | --- | --- |--- |
| 初级 | 组件团队，缺乏定期的交流制度 | 没有清晰的需求管理体系 | 每个版本的开发周期长，无法快速响应需求的变化 |
| 中级 | 全功能的特性团队，每日站立会议 | 定义了产品待办项和迭代待办项 | 采用了迭代开发，定期交付小版本 |
| 高级 | 自组织的特性团队，团队成员定期轮换，形成知识共享 | 建立了故事地图、建立了史诗故事、特性与用户故事的需求体系 | 建立了可视化的看板，由下游拉动需求的开发，消除浪费 |
#### 2.领域建模能力
| 等级 | 建模方式 |
| --- | --- |
| 初级 | 采用数据建模，建立以数据表关系为基础的数据模型 |
| 中级 | 采用领域建模，建模工作只限于少数资深技术人员，并凭借经验完成建模  |
| 高级 | 采用事件风暴、四色建模等建模方法，由领域专家与开发团队一起围绕核心子领域开展领域建模 |
#### 3.架构设计能力
| 等级 | 架构 | 设计 |
| --- | --- | --- |
| 初级 | 采用传统三层架构，未遵循整洁架构，整个系统缺乏清晰的边界 | 采用贫血领域模型，业务逻辑主要以事务脚本实现 | 
| 中级 | 领域层作为分层架构的独立一层，并为领域层划分了模块 | 采用了富领域模型，遵循面向对象设计思想，但未明确定义聚合和资源库 |
| 高级 | 建立了系统层次与限界上下文层次的系统架构，遵循了整洁架构，建立了清晰的限界上下文与领域层边界 | 建立了以聚合为核心的领域设计模型，职责合理地分配给聚合、资源库与领域服务 |
#### 4.整洁编码能力
| 等级 | 编码 | 自动化测试 |
| --- | --- | --- |
| 初级 | 编码以实现功能为唯一目的 | 没有任何自动化测试 | 
| 中级 | 方法和类的命名都遵循了统一语言，可读性高 | 为核心的领域产品代码提供了单元测试 |
| 高级 | 采用测试驱动开发编写领域代码，遵循简单设计原则 | 具有明确的测试战略，单元测试先行 |

### 八、结束语
对我们程序员而言，一件事**是不是有技术含量**往往不取决于事情本身，而**取决于我们怎么做它**。换言之，问题是一样的，但不同的解决方案却会带来不同的效果。产品提出的是问题，我们给出的是解决方案。
### 九、推荐
* 架构设计能力: 
    1. [《许式伟的架构课》](https://time.geekbang.org/column/intro/166)
    2. [《软件设计之美》](https://time.geekbang.org/column/intro/313)
    3. [《架构实战案例解析》](https://time.geekbang.org/column/intro/281)
    4. [《领域驱动设计实践(战略+战术)》](https://gitbook.cn/gitchat/column/5cdab7fb34b6ed1398fd8de7)
    5. [《DDD实战课》](https://time.geekbang.org/column/intro/238)
    6. 《架构整洁之道》
    7. 《架构之美》
    8. 《企业应用架构模式》
    9. 《实现领域驱动设计》
    10. 《领域驱动设计与模式实战》
    
* 整洁编码能力: 
    1. [《设计模式之美》](https://time.geekbang.org/column/intro/250)
    2. [《代码精进之路》](https://time.geekbang.org/column/intro/129)
    3. [《数据结构与算法之美》](https://time.geekbang.org/column/intro/126)
    4. [《编译原理之美》](https://time.geekbang.org/column/intro/219)
    5. [《编译原理实战课》](https://time.geekbang.org/column/intro/314)
    6. 《深入浅出面向对象分析与设计》
    7. 《Effective Java》
    8. 《重构 改善既有代码的设计》
    9. 《代码整洁之道》