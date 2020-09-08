[![Build Status](https://travis-ci.org/Jxin-Cai/mdd.svg?branch=data-mdd)](https://travis-ci.org/Jxin-Cai/mdd)
[![codecov](https://codecov.io/gh/Jxin-Cai/mdd/branch/data-mdd/graph/badge.svg)](https://codecov.io/gh/Jxin-Cai/mdd/)
### 项目介绍

data-mdd分支是数据模型驱动设计的实现。当前项目题材是[首届云原生编程挑战赛(复赛)](https://code.aliyun.com/middleware-contest-2020/mini-faas)。


### 需求描述

一个简化的FaaS系统分为APIServer，Scheduler，ResourceManager，NodeService，ContainerService 5个组件，本题目中APIServer，ResourceManager，NodeService，ContainerService由平台提供，Scheduler的```AcquireContainer```和```ReturnContainer```API由选手实现（gRPC服务，语言不限），在本赛题中Scheduler```无需考虑分布式多实例问题，Scheduler以容器方式单实例运行```。

![arch](https://cdn.nlark.com/yuque/0/2020/png/597042/1594746644546-abb9fa4e-e785-4d0c-9b60-a2049e66683b.png)

由上图我们可以看到，Scheduler会调用ResourceManager和NodeService的API，被APIServer调用，图中连线描述表示可以调用的API。

其中测试函数由平台提供，可能包含但不局限于helloworld，CPU intensive，内存intensive，sleep等类型；调用模式包括稀疏调用，密集调用，周期调用等；执行时间包括时长基本固定，和因输入而异等。

选手对函数的实现无感知，可以通过Scheduler的`AcquireContainer`和`ReturnContainer` API的调用情况，以及`NodeService.GetStats` API获得一些信息，用于设计和实现调度策略。
1. 通过`AcquireContainer`的调用反映了InvokeFunction调用的开始时间。
2. `ReturnContainer`的调用包含了请求级别的函数执行时间。
3. `NodeService.GetStats`返回了Container级别的资源使用情况和函数执行时间统计。


下面是各个组件的介绍：

**APIServer**：评测程序调用`APIServer.InvokeFunction`执行函数。

  * ListFunctions：返回所有可调用Function信息。本题目会预先定义一些Function，选手无需自行创建Function。
  * InvokeFunction(Function，Event)：执行某个Function，传入Event。

**Scheduler**：Scheduler管理系统的Container。APIServer通过Scheduler获得可以执行Function的Container。

  * AcquireContainer(Function)：获得一个可以执行Function的Container。如果系统有Container可以运行Function，Scheduler可以直接返回Container，否则Scheduler需要通过ResourceManager获得Node，进而在Node上创建Container。
  * ReturnContainer(Container)：归还Container。APIServer在调用`NodeService.InvokeFunction`后会调用ReturnContainer归还Container。Scheduler可以利用`AcquireContainer`和`ReturnContainer`记录Container的使用情况。

**ResourceManage**r：ResourceManager管理系统里的Node，负责申请和释放。可以认为Node对应于虚拟机，占用虚拟机需要一定的成本，因此Scheduler的一个目标是如何最大化的利用Node，比如尽量创建足够多的Container，不用的Node应该尽快释放。当然申请和释放Node又需要一定的时间，造成延迟增加，Scheduler需要平衡延迟和资源使用时间。

  * ReserveNode：申请一个Node，该API返回一个Node地址，使用该地址可以创建Container或者销毁Container。
  * ReleaseNode：释放Node。

**NodeService**：NodeService管理单个Node上创建和销毁Container，Node可以认为是一个虚拟机，Scheduler可以在Node上创建用于执行Function的Container。

  * CreateContainer(Function)：创建Container，该API返回Node地址和Container ID，使用改地址可以InvokeFunction。Scheduler可以通过`Node.DestroyContainer`销毁Container。
  * RemoveContainer(Container)：销毁Container。
  * InvokeFunction(Container, Event)：执行Node上的某个Container所加载的函数。
  * GetStats()：返回该Node下所有Container和执行环境相关指标，比如CPU，内存。

**ContainerService**：ContainerService用于执行函数。**注意，一个为某个Function创建的Container不能用来执行其它函数，但是可以串行或者并行执行同一个函数**。Scheduler不直接与ContainerService交互。

  * InvokeFunction(Event)：执行函数。

下面的时序图描述了3种常见场景下各组件调用关系：

![faas-sequence](https://cdn.nlark.com/yuque/0/2020/png/597042/1594747370103-03556b1e-c73a-4fe5-9205-e048dd7c200b.png) 

1. 当Client发起`InvokeFunction`请求时，APIServer调用Scheduler获得执行函数所需要的Container，Scheduler发现没有Node可用，因此调用ResourceManager预留Node，然后使用NodeService创建Container，返回Node地址和Container信息给APIServer。APIServer通过`NodeService.InvokeFunction`执行函数，返回结果给Client端，APIServer通过ReturnContainer告诉Scheduler该Container使用完毕，Scheduler可以决定是释放该Container或者将该Container处理下一个请求。注意：每个Container只能执行同一个函数。
2. 当Client调用同一个函数时，APIServer调用Scheduler获得执行函数所需要的Container，Scheduler发现已经为该函数创建过Container，所以直接将Node地址和Container信息返回给APIServer，之后的调用步骤与上面的场景一致。注意：如果APIServer在尚未将Container归还给Scheduler时，Scheduler是否可以将该Container返回给APIServer呢？一般来说，这取决于这个Container是否有资源来处理多个请求。这是Scheduler需要探索的地方之一。
3. 当Client调用另一个函数时，APIServer调用Scheduler获得执行函数所需要的Container，Scheduler发现该函数没有可用的Container，但是Node上还可以创建更多的Container，因此它调用NodeService为这个函数创建新的Container，并返回Node地址和Container信息给APIServer，之后的调用步骤与上面的场景一致。

### 数据模型驱动设计

#### 一、数据模型分析阶段
##### 1.关键事件
* 获取容器: 通过funcName获取已存在的容器
* 创建容器: 通过nodeId获取已存在的node
* 清理节点: 通过nodeId获取node
* 清理容器: 通过nodeId获取node上的容器
* 扩容容器: 通过funcName获取函数配置信息(内存大小)
* 扩容节点: 申请新的节点
##### 2.实体关系模型
(1) 初步推导出实体关系模型

![实体关系模型一](https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/entity_relation_model_01.png)

(2) 因为需求功能无需func和node间的关联关系,故精简实体关系模型

![实体关系模型二](https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/entity_relation_model_02.png)
##### 3.数据项模型
(1) 理想的数据项与实体关系模型

![理想的数据项与实体关系模型](https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/ideal_data_model.png)

(2) faas的数据项模型

![faas的数据项模型](https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/data_item_model_01.png)

#### 二、数据模型设计阶段
##### 1.数据项模型优化
(1) <冗余字段>为了简化系统逻辑,减少关联表查询。对container表做冗余操作。冗余node字段,以实现返回addr和port数据和序号优先;冗余func字段,以实现请求时直接映射到目标容器集,以及请求返回超时时自动释放容器。

![faas的新数据项模型](https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/data_item_model_02.png)

(2) <减少关联>冗余后,实体关系模型也发生了变化。container的所有功能无需再关联另外两张表实现。三张表无需再存在关联关系。
* container负责核心调度逻辑的业务
* node在需要新增container时提供持有的nodeInfo
* func在需要扩容某个func的container时,提供funcInfo

![实体关系模型三](https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/entity_relation_model_03.png)

##### 2.仓储层设计
(1) 面向人的接口规格设计(面向意图)

![数据访问层设计](https://raw.githubusercontent.com/Jxin-Cai/photo/master/mdd/data/repository_model.png)
#### 三、数据模型实现阶段
##### 1.ORM映射
##### 2.分层
##### 3.红线
* 技术代码与业务代码分离
* 严格准守分层
* 功能相关的逻辑尽量收敛在业务代码

### 总结

* 持久化对象就是数据表的映射,并合理处理数据表之间的关系
* 数据访问对象负责与数据库的交互
* 服务利用事务脚本组织业务过程

### MDD(度量驱动开发)
#### 一、性能指标
#### 二、业务指标

### TDD

### 推荐
Martin Fowler: 《企业应用架构模式》
阿里巴巴: 《码出高效》



### 技术栈
本项目的开发基于Java语言进行开发，具体环境包括：

```
Java: Java 8+
Maven: 3
Spring Boot: 2.2.7.RELEASE
MyBatis: (跟Spring Boot)
HikariCp: (跟Spring Boot)
H2: (跟Spring Boot)
Flywaydb: (跟Spring Boot)
Grpc: 2.9.0.RELEASE
```

我个人更喜欢用MyBatis。因为使用这种半自动ORM框架能更好的解耦"技术代码"。并且依赖Idea的插件。大部分代码都可以简便的生成,实际的开发效率并不弱于其他全自动的ORM框架。全自动ORM框架或多或少都有一定"技术代码"耦合,在将来变更时会比较被动,且不符合我个人对代码的追求。

在《领域驱动设计与模式实战》一书中，Jimmy Nilsson 总结了如下特征，他认为这些特征违背了持久化透明的原则:

* 从特定的基类（Object 除外）进行继承
* 只通过提供的工厂进行实例化
* 使用专门提供的数据类型实现特定接口
* 提供专门的构造方法
* 提供必需的特定字段
* 避免某些结构或强制使用某些结构