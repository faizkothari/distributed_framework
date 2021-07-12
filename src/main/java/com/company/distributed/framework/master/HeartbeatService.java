package com.company.distributed.framework.master;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class HeartbeatService
{
    private final int m_port;
    private Thread m_hbServerThread;

    HeartbeatService(int port)
    {
        m_port = port;
    }

    void startService() throws Exception
    {
        if (m_hbServerThread != null)
        {
            return;
        }

        Thread m_hbServerThread = new Thread(new HeartbeatServer(new DatagramSocket(m_port)), "heartbeat-server");

        m_hbServerThread.start();
    }

    void stopService()
    {
        m_hbServerThread.interrupt();
        m_hbServerThread = null;
    }

    private static class HeartbeatServer implements Runnable
    {
        private DatagramSocket m_socket;
        private byte[] buf = new byte[256];

        HeartbeatServer(DatagramSocket socket)
        {
            m_socket = socket;
        }

        @Override
        public void run()
        {
            try
            {
                startReceiverLoop();
            }
            catch (Exception e)
            {
                System.err.println("Exception caught in heartbeat receive loop: " + e.getMessage());
            }
            finally
            {
                m_socket.close();
            }
        }

        void startReceiverLoop() throws Exception
        {
            Thread currThread = Thread.currentThread();

            System.out.println("Started heartbeat service.");

            while (true)
            {
                if (currThread.isInterrupted())
                {
                    return;
                }

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                m_socket.receive(packet);
                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getData().length);

                bb.order(ByteOrder.LITTLE_ENDIAN);
                long workerHeartbeatTimeMillis = bb.getLong();
                short workerIdLen = bb.getShort();
                System.out.println(workerIdLen);
                String workerId = new String(bb.array(), bb.position(), workerIdLen, StandardCharsets.UTF_8);

                System.out.println("Received heartbeat for " + workerId + " time: " + new Date(workerHeartbeatTimeMillis));
            }
        }
    }
}
