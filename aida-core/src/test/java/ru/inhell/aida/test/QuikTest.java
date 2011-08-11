package ru.inhell.aida.test;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.StdCallLibrary;
import ru.inhell.aida.quik.Trans2Quik;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.03.11 15:51
 */
public class QuikTest {
    public static void main(String... agrs){

        //Create and set up the window.
        JFrame frame = new JFrame("Trans2Quik");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton connect = new JButton("CONNECT");
        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LongByReference pnExtendedErrorCode = new LongByReference();
                byte[] lpstrErrorMessage = new byte[255];

                NativeLong code = Trans2Quik.INSTANCE.TRANS2QUIK_CONNECT("C:\\Anatoly\\QUIK\\", pnExtendedErrorCode,
                        lpstrErrorMessage, 255);

                System.out.println(code);
                System.out.println(pnExtendedErrorCode.toString());
                System.out.println(Native.toString(lpstrErrorMessage));
            }
        });
        frame.add(connect);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}
