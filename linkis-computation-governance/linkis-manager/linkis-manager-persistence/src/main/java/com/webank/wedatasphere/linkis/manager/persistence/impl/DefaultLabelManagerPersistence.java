package com.webank.wedatasphere.linkis.manager.persistence.impl;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceNode;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceResource;
import com.webank.wedatasphere.linkis.manager.dao.LabelManagerMapper;
import com.webank.wedatasphere.linkis.manager.dao.NodeManagerMapper;
import com.webank.wedatasphere.linkis.manager.entity.Tunple;
import com.webank.wedatasphere.linkis.manager.exception.PersistenceWarnException;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils;
import com.webank.wedatasphere.linkis.manager.persistence.LabelManagerPersistence;
import com.webank.wedatasphere.linkis.manager.util.PersistenceUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;


public class DefaultLabelManagerPersistence implements LabelManagerPersistence {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private LabelManagerMapper labelManagerMapper;

    private NodeManagerMapper nodeManagerMapper;

    public LabelManagerMapper getLabelManagerMapper() {
        return labelManagerMapper;
    }

    public void setLabelManagerMapper(LabelManagerMapper labelManagerMapper) {
        this.labelManagerMapper = labelManagerMapper;
    }

    public NodeManagerMapper getNodeManagerMapper() {
        return nodeManagerMapper;
    }

    public void setNodeManagerMapper(NodeManagerMapper nodeManagerMapper) {
        this.nodeManagerMapper = nodeManagerMapper;
    }

    @Override
    public void addLabel(PersistenceLabel persistenceLabel) {
        labelManagerMapper.registerLabel(persistenceLabel);
        int labelId = persistenceLabel.getId();

        //此处需要修正，要拿到 label_value_key label_value_content  labelValue中有多对参数
        Map<String, String> labelValueKeyAndContent = persistenceLabel.getValue();

        labelManagerMapper.registerLabelKeyValues(labelValueKeyAndContent, labelId);
    }

    @Override
    public void removeLabel(int id) {
        labelManagerMapper.deleteUserById(id);
        labelManagerMapper.deleteLabelKeyVaules(id);
        labelManagerMapper.deleteLabel(id);
    }

    @Override
    public void removeLabel(PersistenceLabel persistenceLabel) {
        String labelKey = persistenceLabel.getLabelKey();
        String labelStringValue = persistenceLabel.getStringValue();
        labelManagerMapper.deleteByLabel(labelKey, labelStringValue);
    }

    @Override
    public void updateLabel(int id, PersistenceLabel persistenceLabel) {
        //1.更新label表
        //2.删掉value
        //3.更新labelValue
        // TODO: 2020/10/12  updateLabel 要重写判空
        persistenceLabel.setUpdateTime(new Date());
        labelManagerMapper.updateLabel(id, persistenceLabel);
        labelManagerMapper.deleteLabelKeyVaules(id);
        if(!persistenceLabel.getValue().isEmpty()){
            labelManagerMapper.registerLabelKeyValues(persistenceLabel.getValue(),id);
        }
    }

    @Override
    public PersistenceLabel getLabel(int id) {
        return labelManagerMapper.getLabel(id);
    }

    @Override
    public List<PersistenceLabel> getLabelByServiceInstance(ServiceInstance serviceInstance) {
        List<PersistenceLabel> persistenceLabelList = labelManagerMapper.getLabelByServiceInstance(serviceInstance.getInstance());
        persistenceLabelList.forEach(PersistenceUtils::setValue);
        return persistenceLabelList;
    }

    @Override
    public List<PersistenceLabel> getLabelByResource(PersistenceResource persistenceResource) {
        List<PersistenceLabel> persistenceLabelList = labelManagerMapper.getLabelByResource(persistenceResource);
        return persistenceLabelList;
    }

    @Override
    public void addLabelToNode(ServiceInstance serviceInstance, List<Integer> labelIds) {
        if(!CollectionUtils.isEmpty(labelIds)){
            labelManagerMapper.addLabelServiceInstance(serviceInstance.getInstance(), labelIds);
        }
    }

    @Override
    public List<PersistenceLabel> getLabelsByValue(Map<String, String> value, Label.ValueRelation valueRelation) {
        return getLabelsByValueList(Collections.singletonList(value), valueRelation);
    }

    @Override
    public List<PersistenceLabel> getLabelsByValueList(List<Map<String, String>> valueList, Label.ValueRelation valueRelation) {
        if (PersistenceUtils.valueListIsEmpty(valueList)) return Collections.emptyList();
        if (valueRelation == null) valueRelation = Label.ValueRelation.ALL;
        return labelManagerMapper.dimListLabelByValueList(PersistenceUtils.filterEmptyValueList(valueList), valueRelation.name()).stream().map(PersistenceUtils::setValue).collect(Collectors.toList());
    }

    @Override
    public PersistenceLabel getLabelsByKeyValue(String labelKey, Map<String, String> value, Label.ValueRelation valueRelation) {
        List<PersistenceLabel> labelsByValueList = getLabelsByKeyValueMap(Collections.singletonMap(labelKey, value), valueRelation);
        return labelsByValueList.isEmpty() ? null : labelsByValueList.get(0);
    }

    @Override
    public List<PersistenceLabel> getLabelsByKeyValueMap(Map<String, Map<String, String>> keyValueMap, Label.ValueRelation valueRelation) {
        if (PersistenceUtils.KeyValueMapIsEmpty(keyValueMap)) return Collections.emptyList();
        if (valueRelation == null) valueRelation = Label.ValueRelation.ALL;
        return labelManagerMapper.dimListLabelByKeyValueMap(PersistenceUtils.filterEmptyKeyValueMap(keyValueMap), valueRelation.name()).stream().map(PersistenceUtils::setValue).collect(Collectors.toList());
    }


    @Override
    public List<PersistenceLabel> getLabelsByKey(String labelKey) {
        List<PersistenceLabel> persistenceLabelList = labelManagerMapper.getLabelsByLabelKey(labelKey);
        return persistenceLabelList;
    }

//    public List<PersistenceLabel> getLabelsByValue(Map<String, String> labelKeyValues) {
//        //先查id再由id得到标签
//        List<Integer> labelIds = labelManagerMapper.getLabelByLabelKeyValues(labelKeyValues);
//        List<PersistenceLabel> persistenceLabelList = labelManagerMapper.getLabelsByLabelIds(labelIds);
//        return persistenceLabelList;
//    }

    @Override
    public void removeNodeLabels(ServiceInstance serviceInstance, List<Integer> labelIds) {
        String instance = serviceInstance.getInstance();
        if (null != labelIds && !labelIds.isEmpty()) {
            labelManagerMapper.deleteLabelIdsAndInstance(instance, labelIds);
        }
    }

    @Override
    public List<ServiceInstance> getNodeByLabel(int labelId) {
        List<PersistenceNode> persistenceNodeList = labelManagerMapper.getInstanceByLabelId(labelId);
        List<ServiceInstance> serviceInstanceList = new ArrayList<>();
        for (PersistenceNode persistenceNode : persistenceNodeList) {
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstance.setInstance(persistenceNode.getInstance());
            serviceInstance.setApplicationName(persistenceNode.getName());
            serviceInstanceList.add(serviceInstance);
        }
        return serviceInstanceList;
    }

    @Override
    public List<ServiceInstance> getNodeByLabels(List<Integer> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) return Collections.emptyList();
        List<String> instances = labelManagerMapper.getInstanceIdsByLabelIds(labelIds);
        List<PersistenceNode> persistenceNodeList = nodeManagerMapper.getNodesByInstances(instances);

        List<ServiceInstance> serviceInstanceList = new ArrayList<>();
        for (PersistenceNode persistenceNode : persistenceNodeList) {
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstance.setInstance(persistenceNode.getInstance());
            serviceInstance.setApplicationName(persistenceNode.getName());
            serviceInstanceList.add(serviceInstance);
        }
        return serviceInstanceList;
    }

    @Override
    public void addLabelToUser(String userName, List<Integer> labelIds) {
        labelManagerMapper.addLabelsByUser(userName, labelIds);
    }

    @Override
    public void removeLabelFromUser(String userName, List<Integer> labelIds) {
        labelManagerMapper.deleteLabelIdsByUser(userName, labelIds);
    }

    @Override
    public List<String> getUserByLabel(int label) {
        List<String> userNames = labelManagerMapper.getUserNameByLabelId(label);
        return userNames;
    }

    @Override
    public List<String> getUserByLabels(List<Integer> labelIds) {
        List<String> userNames = labelManagerMapper.getUserNamesByLabelIds(labelIds);
        return userNames;
    }

    @Override
    public List<PersistenceLabel> getLabelsByUser(String userName) {
        List<PersistenceLabel> persistenceLabelList = labelManagerMapper.getLabelsByUser(userName);
        //to do sure actual type
        return persistenceLabelList;
    }

    @Override
    public Map<PersistenceLabel, List<ServiceInstance>> getNodeRelationsByLabels(List<PersistenceLabel> persistenceLabels) {
        //空集合过滤& 转换
        if (PersistenceUtils.persistenceLabelListIsEmpty(persistenceLabels)) return Collections.emptyMap();
        Map<String, Map<String, String>> keyValueMap = PersistenceUtils.filterEmptyPersistenceLabelList(persistenceLabels).stream().collect(Collectors.toMap(PersistenceLabel::getLabelKey, PersistenceLabel::getValue));
        try {
            String dimType = Label.ValueRelation.ALL.name();
            List<Map<String, Object>> nodeRelationsByLabels = labelManagerMapper.dimListNodeRelationsByKeyValueMap(keyValueMap, dimType);
            List<Tunple<PersistenceLabel, ServiceInstance>> arrays = new ArrayList<Tunple<PersistenceLabel, ServiceInstance>>();
            for (Map<String, Object> nodeRelationsByLabel : nodeRelationsByLabels) {
                ServiceInstance serviceInstance = new ServiceInstance();
                PersistenceLabel persistenceLabel = new PersistenceLabel();
                BeanUtils.populate(serviceInstance, nodeRelationsByLabel);
                BeanUtils.populate(persistenceLabel, nodeRelationsByLabel);
                PersistenceUtils.setValue(persistenceLabel);
                arrays.add(new Tunple(persistenceLabel, serviceInstance));
            }
            return arrays.stream()
                    .collect(Collectors.groupingBy(Tunple::getKey)).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, f -> f.getValue().stream().map(Tunple::getValue).collect(Collectors.toList())));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new PersistenceWarnException(10000, "beanutils populate failed", e);
        }
    }

    @Override
    public Map<ServiceInstance, List<PersistenceLabel>> getLabelRelationsByServiceInstance(List<ServiceInstance> serviceInstances) {
        if (CollectionUtils.isEmpty(serviceInstances)) return Collections.emptyMap();
        try {
            List<Map<String, Object>> nodeRelationsByLabels = labelManagerMapper.listLabelRelationByServiceInstance(serviceInstances);
            List<Tunple<ServiceInstance, PersistenceLabel>> arrays = new ArrayList<Tunple<ServiceInstance, PersistenceLabel>>();
            for (Map<String, Object> nodeRelationsByLabel : nodeRelationsByLabels) {
                ServiceInstance serviceInstance = new ServiceInstance();
                PersistenceLabel persistenceLabel = new PersistenceLabel();
                BeanUtils.populate(serviceInstance, nodeRelationsByLabel);
                BeanUtils.populate(persistenceLabel, nodeRelationsByLabel);
                PersistenceUtils.setValue(persistenceLabel);
                arrays.add(new Tunple(serviceInstance, persistenceLabel));
            }
            return arrays.stream()
                    .collect(Collectors.groupingBy(Tunple::getKey)).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, f -> f.getValue().stream().map(Tunple::getValue).collect(Collectors.toList())));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new PersistenceWarnException(10000, "beanutils populate failed", e);
        }
    }

    @Override
    public PersistenceLabel getLabelByKeyValue(String labelKey, String stringValue) {
        return labelManagerMapper.getLabelByKeyValue(labelKey, stringValue);
    }

    @Override
    public List<ServiceInstance> getNodeByLabelKeyValue(String labelKey, String stringValue) {
        return labelManagerMapper.getNodeByLabelKeyValue(labelKey, stringValue);
    }

}
