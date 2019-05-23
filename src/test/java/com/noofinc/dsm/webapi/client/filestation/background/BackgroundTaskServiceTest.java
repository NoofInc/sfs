package com.noofinc.dsm.webapi.client.filestation.background;

import com.noofinc.dsm.webapi.client.AbstractTest;
import com.noofinc.dsm.webapi.client.filestation.delete.DeleteService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;


public class BackgroundTaskServiceTest extends AbstractTest {

    @Autowired
    BackgroundTaskService backgroundTaskService;

    @Autowired
    DeleteService deleteService;

    @Before
    public void beforeEach() {
        //establish some finished background tasks via file delete service
        deleteService.synchronousDelete("/noofinc-ws-it/bogus-1", false, Optional.empty());
        deleteService.synchronousDelete("/noofinc-ws-it/bogus-2", false, Optional.empty());
    }

    @Test
    public void testList() {
        BackgroundTask.TaskList list = backgroundTaskService.list();
        assertTrue(list.getElements().size() >= 2);
    }

    @Test
    public void testClear() {
        BackgroundTask.TaskList list = backgroundTaskService.list();
        int sizeBefore = list.getElements().size();
        assertTrue(list.getElements().size() >= 2);

        backgroundTaskService.clear(Arrays.asList(list.getElements().get(0).taskId, list.getElements().get(1).taskId));
        list = backgroundTaskService.list();
        int sizeAfter = list.getElements().size();
        assertEquals(sizeAfter, sizeBefore - 2);
    }

    @Test
    public void testClearAll() {
        BackgroundTask.TaskList list = backgroundTaskService.list();
        int sizeBefore = list.getElements().size();
        assertTrue(list.getElements().size() >= 2);

        backgroundTaskService.clear();
        list = backgroundTaskService.list();
        int sizeAfter = list.getElements().size();
        assertEquals(sizeAfter, 0);
    }

    @Test
    public void testClear_null_task_list() {
        BackgroundTask.TaskList list = backgroundTaskService.list();
        int sizeBefore = list.getElements().size();
        assertTrue(list.getElements().size() >= 2);

        //null list of taskids
        backgroundTaskService.clear(null);
        list = backgroundTaskService.list();
        int sizeAfter = list.getElements().size();
        assertEquals(sizeAfter, 0);
    }

    @Test
    public void testClear_empty_task_list() {
        BackgroundTask.TaskList list = backgroundTaskService.list();
        int sizeBefore = list.getElements().size();
        assertTrue(list.getElements().size() >= 2);

        //empty list of taskids
        backgroundTaskService.clear(new ArrayList<String>());
        list = backgroundTaskService.list();
        int sizeAfter = list.getElements().size();
        assertEquals(sizeAfter, 0);
    }

}
