package com.jxin.faas.scheduler.domain.container.repository.persistence.impl;

import com.jxin.faas.scheduler.domain.container.repository.mapper.ContainerMapper;
import com.jxin.faas.scheduler.domain.container.repository.persistence.IContainerRepository;
import com.jxin.faas.scheduler.domain.container.repository.table.ContainerDO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 容器持久层
 * @author Jxin
 * @version 1.0
 * @since 2020/9/3 20:35
 */
@Repository
public class ContainerRepository implements IContainerRepository {
    private final ContainerMapper containerMapper;

    @Autowired
    public ContainerRepository(ContainerMapper containerMapper) {
        this.containerMapper = containerMapper;
    }

    @Override
    public void save(ContainerDO container) {
        containerMapper.insertSelective(container);
    }

    @Transactional
    @Override
    public List<String> removeIdleContainer(String nodeId, Date lastReqTime) {
        final List<String> containerIdList =
                containerMapper.findContainerIdByNodeIdAndEnabledAndModifyTimeGreaterThan(nodeId, false, lastReqTime);
        if(CollectionUtils.isEmpty(containerIdList)){
            return containerIdList;
        }
        containerMapper.deleteByContainerIdIn(containerIdList);
        return containerIdList;
    }

    @Override
    public List<String> removeOutTimeContainer(String nodeId, Date outTime) {
        final List<String> containerIdList =
                containerMapper.findContainerIdByNodeIdAndEnabledAndOuttimeLessThan(nodeId, true, outTime);
        if(CollectionUtils.isEmpty(containerIdList)){
            return containerIdList;
        }
        containerMapper.deleteByContainerIdIn(containerIdList);
        return containerIdList;
    }

    @Override
    public void enableContainer(String containerId, Date outTime) {
        final ContainerDO container = new ContainerDO();
        container.setEnabled(true);
        container.setOuttime(outTime);
        containerMapper.updateByContainerId(container, containerId);
    }

    @Override
    public void releaseContainer(String containerId) {
        final ContainerDO container = new ContainerDO();
        container.setEnabled(false);
        containerMapper.updateByContainerId(container, containerId);
    }

    @Transactional
    @Override
    public Optional<ContainerDO> getAndUseContainer(String funcName, Date outTime) {
        final ContainerDO container =
                containerMapper.findByEnabledAndFuncNameOrderByOrder(false, funcName);
        if(container == null){
            return Optional.empty();
        }
        enableContainer(container.getContainerId(), outTime);
        return Optional.of(container);
    }

    @Override
    public int countByNodeId(String nodeId) {
        return containerMapper.countByNodeId(nodeId);
    }

    @Override
    public int countByFuncName(String funcName) {
        return containerMapper.countByFuncName(funcName);
    }
}
