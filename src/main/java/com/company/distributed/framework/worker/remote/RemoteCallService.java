package com.company.distributed.framework.worker.remote;

import com.company.distributed.framework.GetNextTaskResponse;
import com.company.distributed.framework.RemoteCallsGrpc;
import com.company.distributed.framework.WorkerRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class RemoteCallService
{
    private final String m_ipAddr;
    private final int m_port;

    private final ManagedChannel m_channel;
    private final RemoteCallsGrpc.RemoteCallsBlockingStub m_stub;

    public RemoteCallService(String ipAddr, int port)
    {
        m_ipAddr = ipAddr;
        m_port = port;

        m_channel = ManagedChannelBuilder.forAddress(m_ipAddr, m_port)
                .usePlaintext()
                .build();

        m_stub = RemoteCallsGrpc.newBlockingStub(m_channel);
    }

    public void shutdown()
    {
        m_channel.shutdownNow();
    }

    public String sendJoinRequest(String workerId)
    {
        WorkerRequest joinRequest = WorkerRequest.newBuilder()
                .setTimestamp(System.currentTimeMillis()).setWorkerId(workerId).build();

        return m_stub.join(joinRequest).getCode();
    }

    public String sendGetNextTaskRequest(String workerId)
    {
        WorkerRequest getNextTaskRequest = WorkerRequest.newBuilder()
                .setTimestamp(System.currentTimeMillis()).setWorkerId(workerId).build();

        GetNextTaskResponse response = m_stub.getNextTask(getNextTaskRequest);

    }

    public static class NextTask
}
