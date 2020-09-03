/*
drop table if exists container;

drop table if exists func;

drop table if exists node;
*/

/*==============================================================*/
/* Table: container                                             */
/*==============================================================*/
create table container
(
   id                   int(11) not null auto_increment,
   node_id              varchar(30) comment '节点id',
   fun_id               int(11) comment '函数id',
   mem_size             bigint(30) not null default 0 comment '内存大小',
   cpu_usage_ratio      decimal(8,5) not null comment 'cpu使用率',
   create_time          timestamp not null default CURRENT_TIMESTAMP comment '创建时间',
   modify_time          timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
   deleted              tinyint(1) not null default 0 comment '逻辑删除',
   primary key (id)
);

/*==============================================================*/
/* Table: func                                                  */
/*==============================================================*/
create table func
(
   id                   int(11) not null auto_increment,
   name                 varchar(50) not null default '-1' comment '函数名',
   memory_size          bigint(30) not null default 0 comment '内存大小',
   handler              varchar(12) not null default '' comment '执行器',
   timeout              int(11) not null default 0 comment '超时时间 单位ms',
   create_time          timestamp not null default CURRENT_TIMESTAMP comment '创建时间',
   modify_time          timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
   deleted              tinyint(1) not null default 0 comment '逻辑删除',
   primary key (id)
);

/*==============================================================*/
/* Table: node                                                  */
/*==============================================================*/
create table node
(
   id                   int(11) not null auto_increment,
   node_id              varchar(50) not null default '' comment '节点Id',
   address              varchar(50) not null default '-1' comment '地址',
   port                 int(11) not null default 0 comment '端口号',
   idle_mem_size        bigint(30) not null default 0 comment '空闲内存大小',
   total_mem_size       bigint(30) not null default 0 comment '总内存大小',
   cpu_usage_ratio      decimal(8,5) not null comment 'cpu使用率',
   "order"              int(11) not null default 0 comment '顺序',
   create_time          timestamp not null default CURRENT_TIMESTAMP comment '创建时间',
   modify_time          timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
   deleted              tinyint(1) not null default 0 comment '逻辑删除',
   primary key (id)
);
