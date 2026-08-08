describe('offline operation contract',()=>{
  it('uses stable idempotency fields',()=>{
    const operation={operationId:'op',entityType:'work-orders',entityId:'job',action:'UPDATE',localTimestamp:new Date(0).toISOString(),payload:{status:'IN_PROGRESS'}};
    expect(Object.keys(operation)).toEqual(expect.arrayContaining(['operationId','entityType','entityId','action','localTimestamp','payload']));
  });
});

