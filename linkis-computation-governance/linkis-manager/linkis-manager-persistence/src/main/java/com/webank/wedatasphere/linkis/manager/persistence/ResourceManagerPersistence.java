package com.webank.wedatasphere.linkis.manager.persistence;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceResource;
import com.webank.wedatasphere.linkis.manager.exception.PersistenceErrorException;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;

import java.util.List;


public interface ResourceManagerPersistence {
    /**
     * 注册资源
     * @param persistenceResource
     * @throws PersistenceErrorException
     */
    void registerResource(PersistenceResource persistenceResource)  throws PersistenceErrorException;

    void registerResource(ServiceInstance serviceInstance,PersistenceResource persistenceResource)  throws PersistenceErrorException;

    /**
     * 根据标签获取资源
     * @param label
     * @return
     * @throws PersistenceErrorException
     */
    List<PersistenceResource> getResourceByLabel(Label label) throws PersistenceErrorException;

    /**
     * 根据用户获取资源
     * @param user
     * @return
     * @throws PersistenceErrorException
     */
    List<PersistenceResource> getResourceByUser(String user) throws PersistenceErrorException;

    /**
     * 根据serviceinstance 和资源类型获取 资源
     * @param serviceInstance
     * @param resourceType
     * @return
     * @throws PersistenceErrorException
     */
    List<PersistenceResource> getResourceByServiceInstance(ServiceInstance serviceInstance,String resourceType) throws PersistenceErrorException;

    /**
     *根据serviceinstance获取资源
     * @param serviceInstance
     * @return
     * @throws PersistenceErrorException
     */
    List<PersistenceResource> getResourceByServiceInstance(ServiceInstance serviceInstance) throws PersistenceErrorException;

    /**
     * 删除实例占用的资源
     * @param serviceInstance
     * @throws PersistenceErrorException
     */
    void  deleteServiceInstanceResource(ServiceInstance serviceInstance) throws PersistenceErrorException;

    /**
     * 删除过期资源
     * @param ticketId
     * @throws PersistenceErrorException
     */
    void deleteExpiredTicketIdResource(String ticketId) throws PersistenceErrorException;

    /**
     * 更新资源 新EM时候用这个
     * @param serviceInstance
     * @throws PersistenceErrorException
     */
    void nodeResourceUpdate(ServiceInstance serviceInstance,PersistenceResource persistenceResource) throws PersistenceErrorException;
    //resource_id 可能有多条 更新em的时候要注意  过滤出没有 ticketid 的那条，它是em，更新它就行了，更新em用这个方法
    /**
     * 根据ticketId 获取资源
     * @param ticketId
     * @return
     */
    PersistenceResource getNodeResourceByTicketId (String ticketId);

    /**
     * 节点资源更新，更新引擎时候用这个
     * @param ticketId
     * @param persistenceResource
     */
    void nodeResourceUpdate (String ticketId,PersistenceResource persistenceResource);

    List<PersistenceLabel> getLabelsByTicketId(String ticketId);

    void lockResource(List<Integer> labelIds,PersistenceResource persistenceResource);

}
