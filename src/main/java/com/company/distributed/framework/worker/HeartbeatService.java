package com.company.distributed.framework.worker;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HeartbeatService
{
    private final String m_workerId;

    private Thread m_hbClientThread;

    public HeartbeatService(String workerId)
    {
        m_workerId = workerId;
    }

    void startService() throws Exception
    {
        if (m_hbClientThread != null)
        {
            return;
        }

        Thread m_hbServerThread = new Thread(new HeartbeatClient(m_workerId, new DatagramSocket()), "heartbeat-client");

        m_hbServerThread.start();
    }

    void stopService()
    {
        m_hbClientThread.interrupt();
        m_hbClientThread = null;
    }

    private static class HeartbeatClient implements Runnable
    {
        private final String m_workerId;
        private final DatagramSocket m_socket;

        private final ByteBuffer m_bb;

        HeartbeatClient(String workerId, DatagramSocket socket)
        {
            m_workerId = workerId;
            m_socket = socket;

            byte[] workerIdBytes = m_workerId.getBytes(StandardCharsets.UTF_8);

            m_bb = ByteBuffer.allocate(10 + workerIdBytes.length).order(ByteOrder.LITTLE_ENDIAN);
            m_bb.putLong(System.currentTimeMillis());
            m_bb.putShort((short) workerIdBytes.length);
            m_bb.put(workerIdBytes);

            System.out.println(m_bb.capacity());

            m_bb.position(0);
        }

        @Override
        public void run()
        {
            try
            {
                startSenderLoop();
            }
            catch (Exception e)
            {
                System.err.println("Exception caught in heartbeat send loop: " + e.getMessage());
            }
            finally
            {
                m_socket.close();
            }
        }

        void startSenderLoop() throws Exception
        {
            InetAddress address = InetAddress.getByName("localhost");
            Thread currThread = Thread.currentThread();

            System.out.println("Started heartbeat service.");

            while (true)
            {
                if (currThread.isInterrupted())
                {
                    return;
                }

                long currTimeMillis = System.currentTimeMillis();
                m_bb.position(0);
                m_bb.putLong(currTimeMillis);

                DatagramPacket packet = new DatagramPacket(m_bb.array(), m_bb.array().length, address, 8081);
                m_socket.send(packet);

                System.out.println("Sent heartbeat for " + m_workerId + " time: " + new Date(currTimeMillis));

                TimeUnit.SECONDS.sleep(1);
            }
        }
    }
}
