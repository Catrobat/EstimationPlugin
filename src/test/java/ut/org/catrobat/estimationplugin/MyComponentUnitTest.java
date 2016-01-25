package ut.org.catrobat.estimationplugin;

import org.junit.Ignore;
import org.junit.Test;
import org.catrobat.estimationplugin.MyPluginComponent;
import org.catrobat.estimationplugin.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }

    @Test
    public void testAdd()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("Result", 16, component.addNumbers(8, 8));
    }
}