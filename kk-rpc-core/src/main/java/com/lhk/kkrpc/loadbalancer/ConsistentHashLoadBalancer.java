package com.lhk.kkrpc.loadbalancer;

import com.lhk.kkrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性 hash 负载均衡器
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一致性 hash 环，存放虚拟节点
     * TreeMap 保证节点的有序性，会根据节点的 hash 值进行排序
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes= new TreeMap<>();

    /**
     * 单个节点的虚拟节点数量，默认 100
     */
    private final int VIRTUAL_NODE_COUNT = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }

        // 构建一致性 hash 环，每次调用负载均衡器时，都会重新构造 Hash 环，这是为了能够即时处理节点的变化
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
                Integer virtualNodeHash = getHash(serviceMetaInfo.getServiceKey() + "#" + i);
                virtualNodes.put(virtualNodeHash, serviceMetaInfo);
            }
        }

        // 根据请求参数计算 hash 值，在虚拟节点的 hash 值范围内进行查找，找到对应的虚拟节点，返回对应的真实节点
        Integer requestHash = getHash(requestParams);
        // 选择最接近且大于等于调用请求 hash 值的虚拟节点
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(requestHash);
        if (entry == null){
            // 如果没有找到，则选择第一个虚拟节点
            return virtualNodes.firstEntry().getValue();
        }
        return entry.getValue();
    }

    /**
     * 计算 key 的 hash 值
     * @param key
     * @return
     */
    private Integer getHash(Object key) {
        // 可以实现更好的哈希算法
        return key.hashCode();
    }
}
