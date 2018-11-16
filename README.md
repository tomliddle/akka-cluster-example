**Purpose**

This project demonstrates using akka clustering with persistent actors within a cluster singleton

**Akka clustering**

Clustering is used to include 3 clusters currently, although this could be increased easily via the deployment configuration in the Ansible deployment playbook.

Split brain issues are handled by the split brain resolver: https://github.com/mbilski/akka-reasonable-downing

failure-detector.threshold=12 as recommended for AWS deployments: https://doc.akka.io/docs/akka/2.5/cluster-usage.html

Note the persisted class isn't the cluster singeton. It is the child of the cluster singleton as the persisted class needs a supervisor in case it is shutdown due to a persistence failure. The cluster singleton is simple and all it is responsible for is to create the persistent actor and restart if it dies.

The leader of the cluster is not where the cluster singleton is residing. A little bit confusing!

The cluster can run happily on two nodes but not on one.

The database used is DynamoDB - Amazon cloud provided.

