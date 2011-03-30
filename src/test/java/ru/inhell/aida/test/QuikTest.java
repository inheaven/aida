package ru.inhell.aida.test;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
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
                String lpstrErrorMessage = "";
                Integer dwErrorMessageSize = 256;

                NativeLong code = Trans2Quik.INSTANCE.TRANS2QUIK_CONNECT("C:\\Anatoly\\QUIK\\", pnExtendedErrorCode,
                        lpstrErrorMessage, dwErrorMessageSize);

                System.out.println(code);
                System.out.println(pnExtendedErrorCode.getValue());
                System.out.println(lpstrErrorMessage);
                System.out.println(dwErrorMessageSize);
            }
        });
        frame.add(connect);

        //Display the window.
        frame.pack();
        frame.setVisible(true);


    }
}
